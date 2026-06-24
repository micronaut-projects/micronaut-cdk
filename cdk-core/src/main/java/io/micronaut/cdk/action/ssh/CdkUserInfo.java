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
package io.micronaut.cdk.action.ssh;

import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

/**
 * JSCH user info.
 */
public class CdkUserInfo implements UserInfo, UIKeyboardInteractive {

    private final String password;
    private final String passphrase;
    private final boolean trustAllCertificates;

    /**
     * Constructor.
     *
     * @param password             the password
     * @param passphrase           the private key passphrase
     * @param trustAllCertificates whether to trust the identity of unknown hosts
     */
    public CdkUserInfo(String password,
                       String passphrase,
                       boolean trustAllCertificates) {
        this.password = password;
        this.passphrase = passphrase;
        this.trustAllCertificates = trustAllCertificates;
    }

    @Override
    public String getPassphrase() {
        return passphrase;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean promptPassword(String message) {
        return true;
    }

    @Override
    public boolean promptPassphrase(String message) {
        return true;
    }

    @Override
    public boolean promptYesNo(String message) {
        return trustAllCertificates;
    }

    @Override
    public void showMessage(String message) {
    }

    @Override
    public String[] promptKeyboardInteractive(String destination,
                                              String name,
                                              String instruction,
                                              String[] prompt,
                                              boolean[] echo) {
        if (prompt.length != 1 || echo[0] || password == null) {
            return null;
        }
        return new String[]{password};
    }
}
