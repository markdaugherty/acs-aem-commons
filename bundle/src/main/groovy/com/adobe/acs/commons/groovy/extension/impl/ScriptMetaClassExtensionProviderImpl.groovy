package com.adobe.acs.commons.groovy.extension.impl

import com.adobe.acs.commons.groovy.configuration.GroovyScriptConfigurationService
import com.adobe.acs.commons.groovy.context.ScriptContext
import com.adobe.acs.commons.groovy.extension.ScriptMetaClassExtensionProvider
import com.day.cq.replication.ReplicationActionType
import com.day.cq.replication.ReplicationOptions
import com.day.cq.replication.Replicator
import com.day.cq.search.PredicateGroup
import com.day.cq.search.QueryBuilder
import com.day.cq.wcm.api.PageManager
import org.apache.sling.models.factory.ModelFactory
import org.osgi.framework.BundleContext
import org.osgi.service.component.annotations.Activate
import org.osgi.service.component.annotations.Component
import org.osgi.service.component.annotations.Reference

import javax.jcr.Session

@Component(service = ScriptMetaClassExtensionProvider)
class ScriptMetaClassExtensionProviderImpl implements ScriptMetaClassExtensionProvider {

    @Reference
    private Replicator replicator

    @Reference
    private QueryBuilder queryBuilder

    @Reference
    private GroovyScriptConfigurationService configurationService

    private BundleContext bundleContext

    @Override
    Closure<?> getScriptMetaClass(ScriptContext scriptContext) {
        def resourceResolver = scriptContext.resourceResolver
        def session = resourceResolver.adaptTo(Session)
        def pageManager = resourceResolver.adaptTo(PageManager)

        def closure = {
            delegate.getNode = { String path ->
                session.getNode(path)
            }

            delegate.getResource = { String path ->
                resourceResolver.getResource(path)
            }

            delegate.getPage = { String path ->
                pageManager.getPage(path)
            }

            delegate.move = { String src ->
                ["to": { String dst ->
                    session.move(src, dst)
                    session.save()
                }]
            }

            delegate.copy = { String src ->
                ["to": { String dst ->
                    session.workspace.copy(src, dst)
                }]
            }

            delegate.save = {
                session.save()
            }

            delegate.getModel = { String path, Class type ->
                def modelFactoryReference = bundleContext.getServiceReference(ModelFactory)
                def modelFactory = bundleContext.getService(modelFactoryReference)

                def resource = resourceResolver.resolve(path)

                modelFactory.createModel(resource, type)
            }

            delegate.getService = { Class serviceType ->
                def className = serviceType.name
                def service = null

                if (configurationService.allowedServices.any { pattern -> className.matches(pattern) }) {
                    def serviceReference = bundleContext.getServiceReference(className)

                    service = bundleContext.getService(serviceReference)
                }

                service
            }

            delegate.getService = { String className ->
                def service = null

                if (configurationService.allowedServices.any { pattern -> className.matches(pattern) }) {
                    def serviceReference = bundleContext.getServiceReference(className)

                    service = bundleContext.getService(serviceReference)
                }

                service
            }

            delegate.getServices = { Class serviceType, String filter ->
                def serviceReferences = bundleContext.getServiceReferences(serviceType.name, filter)

                serviceReferences.collect { bundleContext.getService(it) }
            }

            delegate.getServices = { String className, String filter ->
                def serviceReferences = bundleContext.getServiceReferences(className, filter)

                serviceReferences.collect { bundleContext.getService(it) }
            }

            delegate.activate = { String path, ReplicationOptions options = null ->
                replicator.replicate(session, ReplicationActionType.ACTIVATE, path, options)
            }

            delegate.deactivate = { String path, ReplicationOptions options = null ->
                replicator.replicate(session, ReplicationActionType.DEACTIVATE, path, options)
            }

            delegate.delete = { String path, ReplicationOptions options = null ->
                replicator.replicate(session, ReplicationActionType.DELETE, path, options)
            }

            delegate.createQuery { Map predicates ->
                queryBuilder.createQuery(PredicateGroup.create(predicates), session)
            }
        }

        closure
    }

    @Activate
    void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
