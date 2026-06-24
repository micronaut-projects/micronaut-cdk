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

import io.micronaut.cdk.action.Spec;
import io.micronaut.cdk.action.SpecReferenceProvider;
import io.micronaut.cdk.annotations.CloudSpecific;
import io.micronaut.cdk.annotations.ResourceSpec;
import io.micronaut.cdk.component.ComponentReference;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Annotation processor for {@link ResourceSpec} annotation. Generates {@link SpecReferenceProvider} implementation
 * based on {@link Spec.Builder} subclass' methods. For each method that accepts a {@link ComponentReference} parameter, or a
 * {@link Collection} of {@link ComponentReference}s parameter on the {@link Spec.Builder}, it tries to find a matching getter
 * method on the {@link Spec} subclass.
 * <p>
 */
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public final class SpecReferencesProcessor extends AbstractProcessor {
    /**
     * Compiler + JVM option that controls debug output.
     */
    public static final String OPT_DEBUG = "cdk.debug.SpecReferencesProcessor";

    /**
     * The default value for the OPT_DEBUG option.
     */
    public static final String DEFAULT_OPT_DEBUG = "false";

    /**
     * Diagnostics; will turn on debugging output during compilation.
     */
    private boolean debug = false;

    /**
     * Resolved element for {@link ComponentReference} type.
     */
    private TypeElement componentReferenceEl;

    /**
     * Resolved element for {@link Collection} type.
     */
    private TypeElement collectionEl;

    /**
     * Resolved element for {@link Spec.Builder} type.
     */
    private TypeElement specBuilderElement;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(ResourceSpec.class.getCanonicalName());
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        debug = Boolean.parseBoolean(processingEnv.getOptions().getOrDefault(OPT_DEBUG,
                System.getProperty(OPT_DEBUG, DEFAULT_OPT_DEBUG)));

        componentReferenceEl = processingEnv.getElementUtils().getTypeElement(ComponentReference.class.getName());
        collectionEl = processingEnv.getElementUtils().getTypeElement(Collection.class.getName());
        specBuilderElement = processingEnv.getElementUtils().getTypeElement(Spec.Builder.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }
        Set<TypeElement> processed = new HashSet<>();
        Deque<Element> toProcess = new ArrayDeque<>(roundEnv.getRootElements());
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
            Optional<? extends AnnotationMirror> specOpt = processingEnv.getElementUtils().getAllAnnotationMirrors(te).stream().filter(
                    am -> am.getAnnotationType().toString().equals(ResourceSpec.class.getCanonicalName())).findAny();
            specOpt.ifPresent(annotationMirror -> processSpecElement(te, annotationMirror));
        }
        return false;
    }

    private void processSpecElement(TypeElement element, AnnotationMirror am) {
        CloudSpecific cs = element.getAnnotation(CloudSpecific.class);
        if (cs == null | element.getModifiers().contains(Modifier.ABSTRACT)) {
            return;
        }
        debugPrintNote("Processing spec element: " + element.getQualifiedName().toString());
        // attempt to find a Spec Builder type. It will be a return type of a builder() static method, or
        // a Builder inner type, by convention:
        Optional<TypeMirror> rt = ElementFilter.methodsIn(element.getEnclosedElements()).stream()
                .filter(m ->
                        m.getModifiers().contains(Modifier.STATIC) &&
                                "builder".contentEquals(m.getSimpleName()))
                .map(ExecutableElement::getReturnType)
                .findAny();

        debugPrintNote("Return type: " + rt.toString());
        TypeElement builderEl = null;
        if (rt.isPresent()) {
            TypeMirror retType = rt.get();
            if (processingEnv.getTypeUtils().isAssignable(retType, specBuilderElement.asType())) {
                // FIXME: move to the filter.
                builderEl = (TypeElement) processingEnv.getTypeUtils().asElement(retType);
                debugPrintNote("Found builder element: " + builderEl.getQualifiedName().toString());
            } else {
                debugPrintNote("Return type is not a builder");
                // try to deduce from type parameters ?
            }
        }
        if (builderEl == null) {
            TypeElement enclosedEl = processingEnv.getElementUtils().getTypeElement(element.getQualifiedName().toString() + ".Builder");
            if (enclosedEl != null && enclosedEl.getKind() == ElementKind.CLASS) {
                builderEl = enclosedEl;
            }
        }
        if (builderEl == null) {
            debugPrintNote("No builder type found for element " + element.getQualifiedName());
            return;
        }

        List<String> singleRefs = new ArrayList<>();
        List<String> collectionRefs = new ArrayList<>();

        Set<ExecutableElement> getters = new HashSet<>();

        for (ExecutableElement method : ElementFilter.methodsIn(builderEl.getEnclosedElements())) {
            for (VariableElement param : method.getParameters()) {
                if (processingEnv.getTypeUtils().isAssignable(param.asType(), componentReferenceEl.asType())) {
                    debugPrintNote("Found ComponentReference parameter: " + method.getSimpleName().toString() +
                            "(" + param.getSimpleName().toString() + ")");
                    // check if there's a getter matching the builder method's name:
                    findMatchingGetter(element, componentReferenceEl.asType(), method.getSimpleName().toString(), true).ifPresent(getters::add);
                } else if (processingEnv.getTypeUtils().isAssignable(
                        processingEnv.getTypeUtils().erasure(param.asType()), collectionEl.asType())) {
                    debugPrintNote("Found Collection parameter: " + method.getSimpleName().toString() +
                            "(" + param.getSimpleName().toString() + ")");
                    DeclaredType dt = (DeclaredType) param.asType();
                    debugPrintNote("Type params: " + dt.getTypeArguments());
                    if (dt.getTypeArguments().size() == 1 &&
                            processingEnv.getTypeUtils().isAssignable(dt.getTypeArguments().get(0), componentReferenceEl.asType())) {
                        TypeMirror specificCollection = processingEnv.getTypeUtils().getDeclaredType(collectionEl, componentReferenceEl.asType());
                        findMatchingGetter(element, specificCollection, /* componentReferenceEl.asType(), */ method.getSimpleName().toString(), false).ifPresent(getters::add);
                    }
                }
            }
        }

        for (ExecutableElement method : getters) {
            if (processingEnv.getTypeUtils().isAssignable(processingEnv.getTypeUtils().erasure(method.getReturnType()), collectionEl.asType())) {
                collectionRefs.add(method.getSimpleName().toString());
            } else {
                singleRefs.add(method.getSimpleName().toString());
            }
        }

        boolean specGenerics = !element.getTypeParameters().isEmpty();

        // generate an implementation of SpecReferenceProvider
        TemplateReferenceProviderImpl rockerTemplate = new TemplateReferenceProviderImpl()
                .packageName("io.micronaut.cdk.impl.processing.references")
                .properties(singleRefs)
                .properties2(collectionRefs)
                .specQName(element.getQualifiedName().toString())
                .cloudEnum(cs.value().name())
                .specGenerics(specGenerics)
                .specName(element.getSimpleName().toString());

        String className = "io.micronaut.cdk.impl.processing.references." + rockerTemplate.specName() + "ReferencesImpl";
        try {
            FileObject fo = processingEnv.getFiler().createSourceFile(className);
            try (OutputStream out = fo.openOutputStream();
                 Writer w = new OutputStreamWriter(out)) {
                w.write(rockerTemplate.render().toString());
            }
        } catch (IOException ex) {
            printError("Error generating factory " + className + ": " + ex.getMessage(), element);
        }
    }

    Optional<ExecutableElement> findMatchingGetter(TypeElement spec, TypeMirror propertyType, String builderMethodName, boolean ignorePlural) {
        String n;
        // TODO extract this to some CodeConventionUtil method, together with other name-convention
        // utilities used in processors.
        if (builderMethodName.startsWith("set") || builderMethodName.startsWith("add")) {
            n = builderMethodName.substring(3);
        } else if (builderMethodName.startsWith("with")) {
            n = builderMethodName.substring(4);
        } else {
            n = builderMethodName;
        }
        debugPrintNote("Base getter name: " + n + ", ignorePlural = " + ignorePlural);
        n = n.toLowerCase(Locale.ENGLISH);
        for (ExecutableElement method : ElementFilter.methodsIn(spec.getEnclosedElements())) {
            String methodName = method.getSimpleName().toString().toLowerCase(Locale.ENGLISH);
            debugPrintNote("Trying: " + methodName + ":" + method.getReturnType() + ", comparing to: " + ("get" + n) + ":" + propertyType);
            if (processingEnv.getTypeUtils().isAssignable(method.getReturnType(), propertyType)) {
                if (("get" + n).equals(methodName) ||
                        ("is" + n).equals(methodName)) {
                    debugPrintNote("Found getter method: " + method.getSimpleName().toString());
                    return Optional.of(method);
                }
            }
            if (ignorePlural && methodName.endsWith("s")) {
                debugPrintNote("Trying -s method: " + method.getSimpleName().toString());
                if (processingEnv.getTypeUtils().isAssignable(
                        processingEnv.getTypeUtils().erasure(method.getReturnType()), collectionEl.asType())) {
                    methodName = methodName.substring(0, methodName.length() - 1);
                    if (("get" + n).equals(methodName) ||
                            ("is" + n).equals(methodName)) {
                        debugPrintNote("Found getter method: " + method.getSimpleName().toString());
                        return Optional.of(method);
                    }
                }
            }
        }
        debugPrintNote("No getter method for: " + n);
        return Optional.empty();
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
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
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
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg, e);
        }
    }
}
