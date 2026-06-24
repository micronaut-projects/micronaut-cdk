/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.cdk.impl.processing;

import io.micronaut.cdk.Cloud;
import io.micronaut.cdk.action.delete.DeleteSpec;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ResourceSpec;
import io.micronaut.cdk.component.Component;
import io.micronaut.cdk.impl.ResourceSpecConverterImpl;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

/**
 * This Processor generates metadata into META-INF/ area of the output. Those are picked up by runtime classes
 * to determine the proper DeleteSpec.Builder for any given Component and/or Spec class.
 * <p>
 * Two forms are generated:
 * <ul>
 *     <li>class.name#method: static method on a class, that ought to accept a (String id, boolean byCDK)</li>
 *     <li>class.name: a Delete.Builder spec class, whose constructor must accept a (String, boolean)</li>
 * </ul>
 * <p>
 * Several deleteBuilder methods are supported. If a Spec class or DeleteSpec class defines {@code deleteBuilder(Component, Spec)},
 * that method is used in preference, without any further logic: its implementation is responsible to decide whether
 * the real data from Component, or declared data from Spec will be used.
 * <p>
 * Then it is decided based on whether Spec is present: if Spec is passed, a deleteBuilder(Spec) is used in preference; the
 * decision is done at runtime by the generated code. If the method does not exist, a deleteBuilder(Component) is used.
 * The least preferred is a deleteBuilder(String, boolean), identifying by CDK/Cloud ID only.
 * Within each category, factory methods are preferred to constructors.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public final class ResourceSpecProcessor extends AbstractProcessor {
    /**
     * Compiler + JVM option that controls debug output.
     */
    public static final String OPT_DEBUG = "cdk.debug.ComponentSpecProcessor";

    /**
     * The default value for the OPT_DEBUG option.
     */
    public static final String DEFAULT_OPT_DEBUG = "false";

    /**
     * Package name where the builder factories will be generated into.
     */
    public static final String PACKAGE_DELETE_FACTORIES = "io.micronaut.cdk.impl.processing.deleteFactories";

    /**
     * Backlog of sources already seen, which were incomplete.
     * Such sources will be added during next round for re-inspection.
     */
    private static final ThreadLocal<Set<String>> INCOMPLETE_SOURCES_BACKLOG = new ThreadLocal<>() {
        @Override
        protected Set<String> initialValue() {
            return new HashSet<>();
        }
    };

    /**
     * Diagnostics; will turn on debugging output during compilation.
     */
    private boolean debug = false;

    /**
     * Records a relationship between a Spec or DeleteSpec and a Component. This data helps in the case that
     * a specialized Spec itself does not record the class of its Component.
     */
    private final Map<TypeMirror, TypeMirror> specToComponent = new HashMap<>();

    /**
     * The specification type. Valid ony for the duration of a single root element processing.
     */
    private TypeElement specType;

    /**
     * The current component type.
     */
    private TypeElement componentType;

    /**
     * True, if delete factory should be generated for the spec.
     */
    private boolean generateDeleteFactory;

    private Map<Cloud, Map<TypeElement, ComponentSpecInfo>> cloudComponents = new HashMap<>();

    /**
     * Constructs the processor.
     */
    public ResourceSpecProcessor() {
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of("*");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        debug = Boolean.parseBoolean(processingEnv.getOptions().getOrDefault(OPT_DEBUG, System.getProperty(OPT_DEBUG, DEFAULT_OPT_DEBUG)));
        debugPrintNote("All roots: " + roundEnv.getRootElements());
        if (roundEnv.processingOver()) {
            flushTypeToComponentMapping();
            flushComponentInfo();
            return false;
        }
        debugPrintNote("ResourceSpecProcessor Round@" + System.identityHashCode(roundEnv) + ": " + roundEnv);
        debugPrintNote("Annotated resources: " + roundEnv.getElementsAnnotatedWith(ResourceSpec.class));

        Set<TypeElement> processed = new HashSet<>();
        Deque<Element> toProcess = new ArrayDeque<>(roundEnv.getRootElements());
        for (String s : INCOMPLETE_SOURCES_BACKLOG.get()) {
            debugPrintNote("Trying backlog type: " + s);
            TypeElement e = processingEnv.getElementUtils().getTypeElement(s);
            if (e == null || e.getKind() != ElementKind.CLASS) {
                debugPrintNote("Throwing away backlog thing that is not a class: " + s);
            } else {
                toProcess.add(e);
            }
        }
        INCOMPLETE_SOURCES_BACKLOG.get().clear();
        while (!toProcess.isEmpty()) {
            Element e = toProcess.removeFirst();
            switch (e.getKind()) {
                case CLASS:
                case INTERFACE:
                case ENUM:
                case PACKAGE:
                case MODULE:
                    toProcess.addAll(e.getEnclosedElements());
                    break;
                default:
                    continue;
            }

            if (!(e instanceof TypeElement te)) {
                continue;
            }
            if (!processed.add(te)) {
                continue;
            }
            if (checkSuperclassChain(te)) {
                debugPrintNote("Class " + te.getQualifiedName() + " is incomplete; adding to backlog");
                INCOMPLETE_SOURCES_BACKLOG.get().add(te.getQualifiedName().toString());
                continue;
            }
            Optional<? extends AnnotationMirror> specOpt = processingEnv.getElementUtils().getAllAnnotationMirrors(te).stream().filter(
                    am -> am.getAnnotationType().toString().equals(ResourceSpec.class.getCanonicalName())).findAny();
            specOpt.ifPresent(annotationMirror -> processSpecElement(te, annotationMirror));
        }
        return false;
    }

    private boolean checkSuperclassChain(TypeElement te) {
        debugPrintNote("Checking superclass chain of " + te.getQualifiedName());
        while (te.getSuperclass() != null) {
            TypeMirror m = te.getSuperclass();
            debugPrintNote("Superclass is " + m + ", kind: " + (m == null ? "0" : m.getKind()));
            if (m == null) {
                return true;
            }
            TypeKind kind = m.getKind();
            if (kind == TypeKind.NONE) {
                return false;
            }
            if (kind != TypeKind.DECLARED) {
                return true;
            }
            te = (TypeElement) ((DeclaredType) m).asElement();
        }
        return false;
    }

    private void processSpecElement(TypeElement element, AnnotationMirror am) {
        this.specType = element;
        this.componentType = null;

        String qName = specType.getQualifiedName().toString();
        PackageElement pkg = processingEnv.getElementUtils().getPackageOf(specType);
        String pkgName = pkg.getQualifiedName().toString();
        TypeMirror deleteClass = null;

        debugPrintNote("Processing " + qName + " of " + pkgName);
        debugPrintNote("annotations: " + specType.getAnnotationMirrors().stream().toList());
        debugPrintNote("ComponentSpec found " + am);
        generateDeleteFactory = true;
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                am.getElementValues().entrySet()) {
            String member = entry.getKey().getSimpleName().toString();
            switch (member) {
                case "component":
                    componentType = (TypeElement) processingEnv.getTypeUtils().asElement((TypeMirror) entry.getValue().getValue());
                    debugPrintNote("Associating spec " + specType + " to component " + componentType.getQualifiedName().toString());
                    break;
                case "deleteSpec":
                    debugPrintNote("DeleteClass specified " + entry.getValue());
                    deleteClass = ((TypeMirror) entry.getValue().getValue());
                    break;
                case "generateDeleteFactory":
                    debugPrintNote("generateDeleteFactory specified " + entry.getValue());
                    this.generateDeleteFactory = (Boolean) entry.getValue().getValue();
                default:
                    break;
            }
        }

        if (componentType == null) {
            printError("Specification is not associated to any Component", specType);
            return;
        }

        registerComponent(specType.asType(), componentType.asType());

        // TODO if we collect annotations from the supertype hierarhcy manually, we can support marker interfaces with
        // @CloudSpecific annotation
        CloudSpecific cs = specType.getAnnotation(CloudSpecific.class);
        String cloudName;

        if (cs == null) {
            // accept & ignore, if the ResourceSpec was inherited.
            TypeMirror resourceAnnotationType = processingEnv.getElementUtils().getTypeElement(ResourceSpec.class.getName()).asType();
            if (specType.getAnnotationMirrors().stream().anyMatch(
                    tm -> processingEnv.getTypeUtils().isSameType(resourceAnnotationType, am.getAnnotationType()))
                    && !specType.getModifiers().contains(Modifier.ABSTRACT)) {
                printError("No cloud specified for " + qName + ", use @CloudSpecific annotation", specType);
            }
        } else {
            cloudName = cs.value().name();
            try {
                generateDeleteFactory(cs.value(), deleteClass, cloudName);
            } catch (IOException e) {
                printError("Error while generating class / metadata: " + e.getMessage(), specType);
            }
        }
    }

    /**
     * Generates the source for the Delete.Builder factory. The generation is driven by {@link ResourceSpec} annotation. If `deleteClass' is not
     * specified explicitly by the annotation, the method attempts to guess the class:
     * <ul>
     *     <li>As enclosing class of the Builder of the deleteBuilder() return type on Spec class</li>
     *     <li>Using naming convention: for pkg.XXXSpec class, look for pkg.XXXDeleteSpec and pkg.cloud.XXXDeleteSpec class</li>
     * </ul>
     * The associated Builder is searched as follows:
     * <ol>
     *     <li>deleteBuilder() static methods on the Spec class are searched; they must return the XXXDeleteSpec.Builder return type</li>
     *     <li>builder() static methods on the XXXDeleteSpec class</li>
     *     <li>constructors of the XXXDeleteSpec.Builder class</li>
     * </ol>
     * From the methods, the most preferred method is picked, taking arguments:
     * <ol>
     *    <li>Spec spec - the specification instance</li>
     *    <li>String id, boolean byCdkId - ID of the created component; Spec.getCdkId() will be used.</li>
     * </ol>
     *
     * @param cloud       the target cloud
     * @param deleteClass the explicitly specified DeleteSpec class from annotation
     * @param cloudName   name of the cloud for the spec
     * @throws IOException if file generation fails
     */
    void generateDeleteFactory(Cloud cloud, TypeMirror deleteClass, String cloudName) throws IOException {
        debugPrintNote("* processing " + specType + " with deleteClass " + deleteClass + " and cloudName " + cloudName);
        MethodInfo.InvocationKind invocationKind = MethodInfo.InvocationKind.NONE;
        ExecutableElement selectedFactory = null;

        TypeElement baseDelSpecElement = processingEnv.getElementUtils().getTypeElement(DeleteSpec.Builder.class.getCanonicalName());
        TypeMirror baseDelSpecType = processingEnv.getTypeUtils().erasure(baseDelSpecElement.asType());

        // the Delete class associated with the Spec
        TypeElement delSpecElement = null;
        TypeElement delBuilderElement = null;

        if (deleteClass != null) {
            TypeElement delClass = (TypeElement) processingEnv.getTypeUtils().asElement(deleteClass);
            TypeElement delBuilder = processingEnv.getElementUtils().getTypeElement(delClass.getQualifiedName().toString() + ".Builder");
            if (delBuilder != null && delBuilder.getKind() == ElementKind.CLASS &&
                    processingEnv.getTypeUtils().isAssignable(delBuilder.asType(), baseDelSpecType)) {
                delSpecElement = delClass;
                delBuilderElement = delBuilder;
                registerComponent(delSpecElement.asType(), componentType.asType());
            } else {
                if (specType.getModifiers().contains(Modifier.ABSTRACT)) {
                    return;
                }
                printError("The specified class " + deleteClass + " is not a DeleteSpec or has no Builder", specType);
                return;
            }
        }

        TypeMirror deleteSpecClass = null;
        String resourceSpecBasename = specType.getSimpleName().toString();
        if (resourceSpecBasename.endsWith("Spec")) {
            resourceSpecBasename = resourceSpecBasename.substring(0, resourceSpecBasename.length() - "Spec".length());
        }
        if (cloudName != null && resourceSpecBasename.substring(0, cloudName.length()).compareToIgnoreCase(cloudName) == 0) {
            resourceSpecBasename = resourceSpecBasename.substring(cloudName.length());
        }

        String cloudCapName = cloudName == null ? "" : cloudName.substring(0, 1).toUpperCase(Locale.ENGLISH) + cloudName.substring(1).toLowerCase(Locale.ENGLISH);

        if (deleteClass == null) {
            debugPrintNote("Guessing deleteClass from static deleteBuilders on " + specType + " with type " + baseDelSpecType);
            List<ExecutableElement> deleteMethods = findStatic(specType, "deletebuilder", baseDelSpecType);
            List<MethodInfo> sm = checkBuilderParameters(deleteMethods, true, componentType, specType);
            if (!sm.isEmpty()) {
                ExecutableElement method = sm.get(0).factoryMethod();
                TypeElement builderEl = (TypeElement) processingEnv.getTypeUtils().asElement(method.getReturnType());
                debugPrintNote("Checking builderElement: " + method + " with return type: " + builderEl);
                if (builderEl.getEnclosingElement() instanceof TypeElement outerT) {
                    debugPrintNote("Enclosing element type: " + processingEnv.getTypeUtils().erasure(outerT.asType()));
                    debugPrintNote("DeleteSpec type: " + processingEnv.getElementUtils().getTypeElement(DeleteSpec.class.getCanonicalName()).asType());
                    if (processingEnv.getTypeUtils().isAssignable(
                            processingEnv.getTypeUtils().erasure(outerT.asType()),
                            processingEnv.getTypeUtils().erasure(processingEnv.getElementUtils().getTypeElement(DeleteSpec.class.getCanonicalName()).asType()))) {
                        debugPrintNote("Guessed deleteFactory: " + sm);
                        selectedFactory = method;
                        invocationKind = sm.get(0).kind();
                        deleteSpecClass = outerT.asType();
                        delSpecElement = outerT;
                        delBuilderElement = builderEl;
                        registerComponent(deleteSpecClass, componentType.asType());
                    } else {
                        printError("The Builder " + builderEl.getQualifiedName() + " must ne an inner class of a DeleteSpec subclass", specType);
                        return;
                    }
                } else {
                    printError("The Builder " + builderEl.getQualifiedName() + " must ne an inner class of a DeleteSpec subclass", specType);
                    return;
                }

            }

            // TODO This block is processed even when deleteClass was guessed.
            // Once excess DeleteSpecs are removed, this legacy support should be undone and the code should skip this block if deleteClass was already guessed.
            if (Boolean.TRUE) {
                // try to guess delete class and the builder:
                // try to guess the relevant DeleteSpec class
                String delClassName = resourceSpecBasename + "DeleteSpec";
                PackageElement specPackage = processingEnv.getElementUtils().getPackageOf(specType);

                Set<String> tryDelClasses = new LinkedHashSet<>();
                // must go first, cloud-specific processing may be there.
                if (cloudName != null) {
                    tryDelClasses.add(specPackage.getQualifiedName().toString() + "." + cloudCapName + delClassName);
                    tryDelClasses.add(specPackage.getQualifiedName().toString() + "." + cloudName.toLowerCase(Locale.ENGLISH) + "." + delClassName);
                    tryDelClasses.add("cdk.action.delete." + cloudName.toLowerCase(Locale.ENGLISH) + "." + delClassName);
                    tryDelClasses.add("cdk.action.delete." + cloudCapName + delClassName);
                }
                tryDelClasses.add(specPackage.getQualifiedName().toString() + "." + delClassName);
                tryDelClasses.add("cdk.action.delete." + delClassName);

                TypeElement candidateDelClass = null;
                for (String qn : tryDelClasses) {
                    debugPrintNote("Trying delete class: " + qn);
                    TypeElement delClass = processingEnv.getElementUtils().getTypeElement(qn);
                    if (delClass != null) {
                        candidateDelClass = delClass;
                        TypeElement delBuilder = processingEnv.getElementUtils().getTypeElement(delClass.getQualifiedName().toString() + ".Builder");
                        debugPrintNote("Checking builder innerclass: " + delBuilder + ", type: " + (delBuilder != null ? processingEnv.getTypeUtils().erasure(delBuilder.asType()) : null) +
                                ", basicDel: " + baseDelSpecType);
                        if (delBuilder != null && delBuilder.getKind() == ElementKind.CLASS &&
                                processingEnv.getTypeUtils().isAssignable(
                                        processingEnv.getTypeUtils().erasure(delBuilder.asType()),
                                        baseDelSpecType
                                )) {
                            // TODO remove if-else statement, once excess DeleteSpecs are removed
                            if (delSpecElement == null) {
                                debugPrintNote("Guessed deleteClass " + delClass + ", with builder " + delBuilder);
                                delSpecElement = delClass;
                                delBuilderElement = delBuilder;
                            } else {
                                debugPrintNote("Adding legacy deleteClass " + delClass + ", with builder " + delBuilder + " into metadata");
                            }
                            registerComponent(delClass.asType(), componentType.asType());
                            break;
                        }
                    }
                }
                if (delBuilderElement == null) {
                    if (specType.getModifiers().contains(Modifier.ABSTRACT)) {
                        return;
                    }
                    if (candidateDelClass != null) {
                        printError("A name-matching " + candidateDelClass + " is not a DeleteSpec or has no Builder", specType);
                    } else {
                        printError("No deleteClass is specified and the delete class could not be found using the name convention (" + String.join(", ", tryDelClasses) + ")", specType);
                    }
                    return;
                }
            }
        }

        if (specType.getModifiers().contains(Modifier.ABSTRACT) || !generateDeleteFactory) {
            return;
        }
        TypeMirror erasedBuilder = processingEnv.getTypeUtils().erasure(delBuilderElement.asType());

        List<ExecutableElement> deleteMethods = findStatic(specType, "deletebuilder", erasedBuilder);
        List<MethodInfo> candidates = checkBuilderParameters(deleteMethods, true, componentType, specType);
        // add static methods from delete spec
        deleteMethods = findStatic(delSpecElement, "builder", erasedBuilder);
        // add constructors from delete spec
        candidates.addAll(checkBuilderParameters(deleteMethods, false, componentType, specType));
        candidates.addAll(checkBuilderParameters(ElementFilter.constructorsIn(delSpecElement.getEnclosedElements()),
                false, componentType, specType));

        if (candidates.isEmpty()) {
            printError("The DeleteSpec class " + delSpecElement.getQualifiedName() + " does not define a builder method or a " +
                    "Builder inner class that matches required signatures", specType);
            return;
        }

        debugPrintNote("* All candidates: " + candidates);
        Collections.sort(candidates);
        debugPrintNote("* Ordered candidates: " + candidates);
        Map<MethodInfo.InvocationKind, MethodInfo> alternatives = new HashMap<>();
        for (MethodInfo mi : candidates) {
            alternatives.putIfAbsent(mi.kind(), mi);
        }

        String specTypeVars = "S" + ", ?".repeat(Math.max(0, specType.getTypeParameters().size() - 1));

        debugPrintNote("Type parameters on Spec class: " + specType.getTypeParameters().toString() + ", resourceGenerocs = ");
        TemplateDeleteComponentBuilderFactory rockerTemplate = new TemplateDeleteComponentBuilderFactory()
                .cloudID(cloudName)
                .componentName(componentType.getSimpleName().toString())
                .componentQN(componentType.getQualifiedName().toString())

                .specName(specType.getSimpleName().toString())
                .specQN(specType.getQualifiedName().toString())
                .specTypes(specTypeVars)

                .deleteSpecName(delSpecElement.getSimpleName().toString())
                .deleteSpecQN(delSpecElement.getQualifiedName().toString())

                .spec(alternatives.get(MethodInfo.InvocationKind.SPEC))
                .comp(alternatives.get(MethodInfo.InvocationKind.COMP))
                .compSpec(alternatives.get(MethodInfo.InvocationKind.COMP_SPEC))
                .cdkId(alternatives.get(MethodInfo.InvocationKind.CDKID))

                .factoryGenerics(candidates.get(0).usesGenerics())
                .packageName(PACKAGE_DELETE_FACTORIES);

        String className = cloudCapName + resourceSpecBasename + "DeleteComponentBuilderFactory";

        rockerTemplate.generatedClassName(className);
        FileObject fo = processingEnv.getFiler().createSourceFile(PACKAGE_DELETE_FACTORIES + "." + className);
        try (OutputStream out = fo.openOutputStream();
             Writer w = new OutputStreamWriter(out)) {
            w.write(rockerTemplate.render().toString());
        }

        registerComponentSpec(cloud, componentType, specType, delSpecElement);
    }

    void registerComponentSpec(Cloud cloud, TypeElement component, TypeElement spec, TypeElement delSpec) {
        if (cloud == null) {
            return;
        }
        cloudComponents.computeIfAbsent(cloud, k -> new HashMap<>())
                .merge(component, new ComponentSpecInfo(component, spec, delSpec),
                        (a, b) -> new ComponentSpecInfo(null, null, null));
    }

    void registerComponent(TypeMirror type, TypeMirror component) {
        specToComponent.merge(type, component, (c1, c2) -> {
            if (!c1.equals(c2)) {
                return processingEnv.getElementUtils().getTypeElement(Component.class.getName()).asType();
            }
            return c1;
        });
    }

    String typeQN(TypeMirror typeMirror) {
        Element e = processingEnv.getTypeUtils().asElement(typeMirror);
        if (!(e instanceof TypeElement te)) {
            return null;
        }
        return te.getQualifiedName().toString();
    }

    void flushComponentInfo() {
        for (Cloud cloud : cloudComponents.keySet()) {
            try {
                Properties props = new Properties();
                for (TypeElement c : cloudComponents.get(cloud).keySet()) {
                    String key = c.getQualifiedName().toString();
                    ComponentSpecInfo specInfo = cloudComponents.get(cloud).get(c);
                    if (specInfo.component == null) {
                        props.setProperty(key, "*");
                    } else {
                        props.setProperty(key, specInfo.specClass.getQualifiedName().toString() + "," + specInfo.deleteSpecClass.getQualifiedName().toString());
                    }
                }
                FileObject fo = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
                        "",
                        String.format(ResourceSpecConverterImpl.COMPONENT_SPEC_RESOURCE, cloud.name()));
                try (OutputStream os = fo.openOutputStream()) {
                    props.store(os, null);
                }
            } catch (IOException ex) {
                printError(
                        "Could not generate type lookup metadata: " + ex.getMessage()
                );
            }
        }
    }

    void flushTypeToComponentMapping() {
        try {
            Properties props = new Properties();
            for (Map.Entry<TypeMirror, TypeMirror> entry : specToComponent.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null) {
                    continue;
                }
                String specQN = typeQN(entry.getKey());
                String componentQN = typeQN(entry.getValue());

                if (specQN == null || componentQN == null) {
                    printError("Could not make FQN from: " + entry.getKey());
                }
                props.setProperty(specQN, componentQN);
            }
            FileObject fo = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
                    "", ResourceSpecConverterImpl.COMPONENT_MAP_RESOURCE);
            try (OutputStream os = fo.openOutputStream()) {
                props.store(os, null);
            }
        } catch (IOException ex) {
            printError(
                    "Could not generate type lookup metadata: " + ex.getMessage()
            );
        }
    }

    private TypeElement methodReturnType(ExecutableElement method) {
        TypeElement enclType = ((TypeElement) method.getEnclosingElement());
        if (method.getKind() == ElementKind.CONSTRUCTOR) {
            return enclType;
        }
        ExecutableType mt = (ExecutableType) processingEnv.getTypeUtils().asMemberOf((DeclaredType) enclType.asType(), method);
        debugPrintNote("Method return type: " + mt);
        if (mt.getReturnType().getKind() == TypeKind.DECLARED) {
            return (TypeElement) processingEnv.getTypeUtils().asElement(mt.getReturnType());
        } else if (mt.getReturnType().getKind() == TypeKind.TYPEVAR) {
            TypeVariable returnTV = (TypeVariable) mt.getReturnType();
            debugPrintNote("Method return type variable: " + returnTV);
            for (TypeVariable tv : mt.getTypeVariables()) {
                if (tv.asElement().getSimpleName().contentEquals(returnTV.asElement().getSimpleName())) {
                    debugPrintNote("found typevar: " + tv);
                    if (tv.getLowerBound() != null && tv.getLowerBound().getKind() == TypeKind.DECLARED) {
                        return (TypeElement) processingEnv.getTypeUtils().asElement(tv.getLowerBound());
                    }
                }
            }
        }
        return null;
    }

    /**
     * Finds and returns methods to be used in the generated factory.
     * <ul>
     *     <li>0 - method that accepts (Component, Spec) and is used in preference</li>
     *     <li>1 - method that accepts Spec</li>
     *     <li>2 - method that accepts Component</li>
     * </ul>
     * If there's a method that accepts (Component, Spec), the method is used in preference, no decision made by the generated factory.
     *
     * @param methods     methods to select from
     * @param compElement the component type
     * @param specClass   true if the class is Spec
     * @param specElement Spec type
     * @return list of suitable methods
     */
    List<MethodInfo> checkBuilderParameters(List<ExecutableElement> methods, boolean specClass, TypeElement compElement, TypeElement specElement) {
        List<MethodInfo> result = new ArrayList<>();
        TypeMirror stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();

        for (ExecutableElement method : methods) {
            debugPrintNote("Checking parameters of: " + method + ", size = " + method.getParameters().size());
            List<? extends VariableElement> params = method.getParameters();
            TypeElement enclType = ((TypeElement) method.getEnclosingElement());
            if (specClass && method.getKind() == ElementKind.CONSTRUCTOR) {
                continue;
            }
            switch (params.size()) {
                case 1:
                    // deleteBuilder(Spec)
                    debugPrintNote("Found parameter: " + params.get(0) + ", type = " + params.get(0).asType());
                    if (isAssignableTo(params.get(0), specElement)) {
                        debugPrintNote("Found builder from Spec", method);
                        result.add(new MethodInfo(
                                enclType.getQualifiedName().toString(),
                                specClass,
                                method,
                                MethodInfo.InvocationKind.SPEC,
                                methodReturnType(method)
                        ));
                    }
                    // deleteBuilder(Component)
                    if (isAssignableTo(params.get(0), compElement)) {
                        debugPrintNote("Found builder from Component", method);
                        result.add(new MethodInfo(
                                enclType.getQualifiedName().toString(),
                                specClass,
                                method,
                                MethodInfo.InvocationKind.COMP,
                                methodReturnType(method)
                        ));
                    }
                    break;
                case 2:
                    // deleteBuilder(Component, Spec)
                    if (isAssignableTo(params.get(0), compElement) && isAssignableTo(params.get(1), specElement)) {
                        debugPrintNote("Found builder from Component+Spec", method);
                        result.add(new MethodInfo(
                                enclType.getQualifiedName().toString(),
                                specClass,
                                method,
                                MethodInfo.InvocationKind.CDKID,
                                methodReturnType(method)
                        ));
                    }
                    // deleteBulder(String, boolean)
                    if (processingEnv.getTypeUtils().isAssignable(params.get(0).asType(), stringType) &&
                            params.get(1).asType().getKind() == TypeKind.BOOLEAN) {
                        debugPrintNote("Found builder from CDK ID", method);
                        result.add(new MethodInfo(
                                enclType.getQualifiedName().toString(),
                                specClass,
                                method,
                                MethodInfo.InvocationKind.CDKID,
                                methodReturnType(method)
                        ));
                    }
                    break;
                default:
                    continue;
            }
        }
        return result;
    }

    private boolean isAssignableTo(VariableElement param, TypeElement type) {
        TypeMirror typeErasure = processingEnv.getTypeUtils().erasure(type.asType());
        boolean match = parameterOf(param).stream().anyMatch(
                t -> processingEnv.getTypeUtils().isAssignable(
                        typeErasure, processingEnv.getTypeUtils().erasure(t)));
        debugPrintNote("Checking " + typeErasure + " is assignable to " + param + ": " + match);
        return match;
    }

    private List<? extends TypeMirror> parameterOf(VariableElement param) {
        TypeMirror paramType = param.asType();
        if (paramType.getKind() == TypeKind.TYPEVAR) {
            for (TypeParameterElement tel : ((ExecutableElement) param.getEnclosingElement()).getTypeParameters()) {
                debugPrintNote("Param: " + paramType + ", typeel: " + tel.getSimpleName() + ", equals: " + param.getSimpleName().contentEquals(tel.getSimpleName()));
                if (paramType.toString().equals(tel.getSimpleName().toString())) {
                    return tel.getBounds();
                }
            }
        }
        return List.of(paramType);
    }

    boolean isStaticBuilderMethod(ExecutableElement exe, String name, TypeMirror returnType) {
        debugPrintNote("Trying: " + exe + " with return type " + exe.getReturnType());
        return exe.getModifiers().contains(Modifier.STATIC) &&
                exe.getSimpleName().toString().toLowerCase().endsWith(name) &&
                processingEnv.getTypeUtils().isAssignable(
                        processingEnv.getTypeUtils().erasure(exe.getReturnType()),
                        processingEnv.getTypeUtils().erasure(returnType)
                );
    }

    List<ExecutableElement> findStatic(TypeElement parent, String name, TypeMirror returnType) {
        debugPrintNote("Searching for static " + name + " on parent " + parent + " with rettype " + returnType);
        List<ExecutableElement> ret = ElementFilter.methodsIn(parent.getEnclosedElements()).stream().filter(exe ->
                isStaticBuilderMethod(exe, name, returnType)
        ).toList();
        debugPrintNote("found: " + ret);
        return ret;
    }

    void printError(CharSequence msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);

    }

    void printError(CharSequence msg, Element e) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }

    /**
     * Optionally prints debugging message.
     *
     * @param msg the message
     */
    void debugPrintNote(CharSequence msg) {
        if (debug) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "ResourceSpecProcessor > " + msg);
        }
    }

    /**
     * Optionally prints debugging message.
     *
     * @param msg the message
     * @param e   the element
     */
    void debugPrintNote(CharSequence msg, Element e) {
        if (debug) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "ResourceSpecProcessor > " + msg, e);
        }
    }

    record ComponentSpecInfo(TypeElement component, TypeElement specClass,
                             TypeElement deleteSpecClass) {
    }
}
