/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.cert.toolkit.model;

public class X500NameInfo {

    private String commonName; // CN

    private String localityName; // L

    private String stateOrProvinceName; // ST

    private String organizationName; // O

    private String organizationalUnitName; // OU

    private String countryName;

    private String streetAddress; // STREET

    private String emailAddress;

    public static X500NameInfo builder() {
        return new X500NameInfo();
    }

    public X500NameInfo commonName(String commonName) {
        this.commonName = commonName;
        return this;
    }

    public X500NameInfo organizationName(String organizationName) {
        this.organizationName = organizationName;
        return this;
    }

    public X500NameInfo organizationalUnitName(String organizationalUnitName) {
        this.organizationalUnitName = organizationalUnitName;
        return this;
    }

    public X500NameInfo countryName(String countryName) {
        this.countryName = countryName;
        return this;
    }

    public X500NameInfo stateOrProvinceName(String stateOrProvinceName) {
        this.stateOrProvinceName = stateOrProvinceName;
        return this;
    }

    public X500NameInfo localityName(String localityName) {
        this.localityName = localityName;
        return this;
    }

    public X500NameInfo email(String email) {
        this.emailAddress = email;
        return this;
    }

    public X500NameInfo build() {
        return this;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getLocalityName() {
        return localityName;
    }

    public void setLocalityName(String localityName) {
        this.localityName = localityName;
    }

    public String getStateOrProvinceName() {
        return stateOrProvinceName;
    }

    public void setStateOrProvinceName(String stateOrProvinceName) {
        this.stateOrProvinceName = stateOrProvinceName;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationalUnitName() {
        return organizationalUnitName;
    }

    public void setOrganizationalUnitName(String organizationalUnitName) {
        this.organizationalUnitName = organizationalUnitName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (commonName != null) {
            builder.append("CN=").append(commonName);
        }
        if (organizationName != null) {
            builder.append(",O=").append(organizationName);
        }
        if (organizationalUnitName != null) {
            builder.append(",OU=").append(organizationalUnitName);
        }
        if (localityName != null) {
            builder.append(",L=").append(localityName);
        }
        if (stateOrProvinceName != null) {
            builder.append(",ST=").append(stateOrProvinceName);
        }
        if (countryName != null) {
            builder.append(",C=").append(countryName);
        }
        if (streetAddress != null) {
            builder.append(",STREET=").append(streetAddress);
        }
        if (streetAddress != null) {
            builder.append(",emailAddress=").append(emailAddress);
        }
        return builder.toString();
    }

}
