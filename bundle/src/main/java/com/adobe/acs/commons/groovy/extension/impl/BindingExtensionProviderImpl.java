package com.adobe.acs.commons.groovy.extension.impl;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import com.adobe.acs.commons.groovy.context.ScriptContext;
import com.adobe.acs.commons.groovy.context.ServletScriptContext;
import com.adobe.acs.commons.groovy.extension.BindingExtensionProvider;
import com.adobe.acs.commons.groovy.extension.BindingVariable;
import com.day.cq.search.QueryBuilder;
import com.day.cq.wcm.api.PageManager;
import groovy.lang.Closure;

@Component(service = BindingExtensionProvider.class)
public final class BindingExtensionProviderImpl implements BindingExtensionProvider {

    @Reference
    private QueryBuilder queryBuilder;

    @Override
    public Map<String, BindingVariable> getBindingVariables(final ScriptContext scriptContext) {
        final ResourceResolver resourceResolver = scriptContext.getResourceResolver();
        final Session session = resourceResolver.adaptTo(Session.class);

        final Map<String, BindingVariable> bindingVariables = new HashMap<>();

        bindingVariables.put("log", new BindingVariable(LoggerFactory.getLogger("groovy")));
        bindingVariables.put("session", new BindingVariable(session, Session.class));
        bindingVariables.put("resourceResolver", new BindingVariable(resourceResolver, ResourceResolver.class));
        bindingVariables.put("pageManager", new BindingVariable(resourceResolver.adaptTo(PageManager.class),
            PageManager.class));
        bindingVariables.put("queryBuilder", new BindingVariable(queryBuilder, QueryBuilder.class));
//        bindingVariables.put("nodeBuilder", null);
//        bindingVariables.put("pageBuilder", null);
        bindingVariables.put("bundleContext", new BindingVariable(session));
        bindingVariables.put("out", new BindingVariable(scriptContext.getPrintStream(), PrintStream.class));

        bindingVariables.put("getNode", new BindingVariable(null, Closure.class));

        if (scriptContext instanceof ServletScriptContext) {
            final ServletScriptContext servletScriptContext = (ServletScriptContext) scriptContext;

            bindingVariables.put("slingRequest", new BindingVariable(servletScriptContext.getRequest(),
                SlingHttpServletRequest.class));
            bindingVariables.put("slingResponse", new BindingVariable(servletScriptContext.getResponse(),
                SlingHttpServletResponse.class));
        }

        if (StringUtils.isNotEmpty(scriptContext.getData())) {
            // TODO parse JSON
            bindingVariables.put("data", new BindingVariable(scriptContext.getData(), String.class));
        }

        return bindingVariables;
    }
}
