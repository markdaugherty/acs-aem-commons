package com.adobe.acs.commons.groovy.extension.impl;

import java.util.Set;

import org.osgi.service.component.annotations.Component;

import com.adobe.acs.commons.groovy.extension.StarImportExtensionProvider;
import com.google.common.collect.ImmutableSet;

@Component(service = StarImportExtensionProvider.class)
public final class StarImportExtensionProviderImpl implements StarImportExtensionProvider {

    private static final Set<String> IMPORTS = ImmutableSet.of(
        "com.day.cq.dam.api",
        "com.day.cq.search",
        "com.day.cq.tagging",
        "com.day.cq.wcm.api",
        "com.day.cq.replication",
        "javax.jcr",
        "org.apache.sling.api",
        "org.apache.sling.api.resource"
    );

    @Override
    public Set<String> getStarImports() {
        return IMPORTS;
    }
}
