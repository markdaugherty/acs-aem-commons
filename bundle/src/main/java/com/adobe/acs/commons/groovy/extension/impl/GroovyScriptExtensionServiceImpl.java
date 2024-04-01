package com.adobe.acs.commons.groovy.extension.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.acs.commons.groovy.context.ScriptContext;
import com.adobe.acs.commons.groovy.extension.BindingExtensionProvider;
import com.adobe.acs.commons.groovy.extension.BindingVariable;
import com.adobe.acs.commons.groovy.extension.CompilationCustomizerExtensionProvider;
import com.adobe.acs.commons.groovy.extension.GroovyScriptExtensionService;
import com.adobe.acs.commons.groovy.extension.ScriptMetaClassExtensionProvider;
import com.adobe.acs.commons.groovy.extension.StarImportExtensionProvider;
import groovy.lang.Closure;

@Component(service = GroovyScriptExtensionService.class)
public final class GroovyScriptExtensionServiceImpl implements GroovyScriptExtensionService {

    private static final Logger LOG = LoggerFactory.getLogger(GroovyScriptExtensionServiceImpl.class);

    private volatile List<BindingExtensionProvider> bindingExtensionProviders = new CopyOnWriteArrayList<>();

    private volatile List<StarImportExtensionProvider> starImportExtensionProviders = new CopyOnWriteArrayList<>();

    private volatile List<ScriptMetaClassExtensionProvider> scriptMetaClassExtensionProviders =
        new CopyOnWriteArrayList<>();

    private volatile List<CompilationCustomizerExtensionProvider> compilationCustomizerExtensionProviders =
        new CopyOnWriteArrayList<>();

    @Override
    public Map<String, BindingVariable> getBindingVariables(final ScriptContext scriptContext) {
        final Map<String, BindingVariable> bindingVariables = new HashMap<>();

        for (final BindingExtensionProvider provider : bindingExtensionProviders) {
            for (final Map.Entry<String, BindingVariable> entry : provider.getBindingVariables(scriptContext)
                .entrySet()) {
                final String name = entry.getKey();

                if (bindingVariables.containsKey(entry.getKey())) {
                    LOG.debug("Binding variable {} is currently bound to value: {}, overriding with value: {}",
                        name, bindingVariables.get(name), entry.getValue().getValue());
                }

                bindingVariables.put(name, entry.getValue());
            }
        }

        return bindingVariables;
    }

    @Override
    public List<CompilationCustomizer> getCompilationCustomizers() {
        final List<CompilationCustomizer> compilationCustomizers = new ArrayList<>();

        compilationCustomizers.add(new ImportCustomizer().addStarImports(getStarImports()
            .toArray(new String[0])));

        for (final CompilationCustomizerExtensionProvider provider : compilationCustomizerExtensionProviders) {
            compilationCustomizers.addAll(provider.getCompilationCustomizers());
        }

        return compilationCustomizers;
    }

    @Override
    public List<Closure<?>> getScriptMetaClasses(final ScriptContext scriptContext) {
        return scriptMetaClassExtensionProviders.stream()
            .map(provider -> provider.getScriptMetaClass(scriptContext))
            .collect(Collectors.toList());
    }

    @Override
    public Set<String> getStarImports() {
        return starImportExtensionProviders.stream()
            .map(StarImportExtensionProvider::getStarImports)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void bindBindingExtensionProvider(BindingExtensionProvider extension) {
        bindingExtensionProviders.add(extension);

        LOG.info("Added binding extension : {}", extension.getClass().getName());
    }

    public void unbindBindingExtensionProvider(BindingExtensionProvider extension) {
        bindingExtensionProviders.remove(extension);

        LOG.info("Removed binding extension : {}", extension.getClass().getName());
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void bindStarImportExtensionProvider(StarImportExtensionProvider extension) {
        starImportExtensionProviders.add(extension);

        LOG.info("Added star import extension : {} with imports : {}", extension.getClass().getName(),
            extension.getStarImports());
    }

    public void unbindStarImportExtensionProvider(StarImportExtensionProvider extension) {
        starImportExtensionProviders.remove(extension);

        LOG.info("Removed star import extension : {} with imports : {}", extension.getClass().getName(),
            extension.getStarImports());
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void bindScriptMetaClassExtensionProvider(ScriptMetaClassExtensionProvider extension) {
        scriptMetaClassExtensionProviders.add(extension);

        LOG.info("Added script metaclass extension : {}", extension.getClass().getName());
    }

    public void unbindScriptMetaClassExtensionProvider(ScriptMetaClassExtensionProvider extension) {
        scriptMetaClassExtensionProviders.remove(extension);

        LOG.info("Removed script metaclass extension : {}", extension.getClass().getName());
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void bindCompilationCustomizerExtensionProvider(CompilationCustomizerExtensionProvider extension) {
        compilationCustomizerExtensionProviders.add(extension);

        LOG.info("Added compilation customizer extension : {}", extension.getClass().getName());
    }

    public void unbindCompilationCustomizerExtensionProvider(CompilationCustomizerExtensionProvider extension) {
        compilationCustomizerExtensionProviders.remove(extension);

        LOG.info("Removed compilation customizer extension : {}", extension.getClass().getName());
    }
}
