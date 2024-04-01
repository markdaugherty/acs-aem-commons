package com.adobe.acs.commons.groovy.audit.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.acs.commons.groovy.audit.AuditRecord;
import com.adobe.acs.commons.groovy.exception.GroovyException;
import com.adobe.acs.commons.groovy.configuration.GroovyScriptConfigurationService;
import com.adobe.acs.commons.groovy.response.RunScriptResponse;
import com.adobe.acs.commons.groovy.audit.GroovyScriptAuditService;
import com.day.cq.commons.jcr.JcrUtil;

import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.AUDIT_NODE_NAME;
import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.AUDIT_PATH;
import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.AUDIT_RECORD_NODE_PREFIX;
import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.DATA;
import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.EXCEPTION_STACK_TRACE;
import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.OUTPUT;
import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.PATH_GROOVY_ROOT;
import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.RESULT;
import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.RUNNING_TIME;
import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.SCRIPT;
import static com.adobe.acs.commons.groovy.constants.GroovyScriptConstants.SUBSERVICE_NAME;
import static com.day.cq.commons.jcr.JcrConstants.MIX_CREATED;
import static com.day.cq.commons.jcr.JcrConstants.NT_UNSTRUCTURED;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component(service = GroovyScriptAuditService.class, immediate = true)
public final class GroovyScriptAuditServiceImpl implements GroovyScriptAuditService {

    private static final Logger LOG = LoggerFactory.getLogger(GroovyScriptAuditServiceImpl.class);

    private static final Map<String, Object> AUTH_INFO = Collections.singletonMap(
        ResourceResolverFactory.SUBSERVICE, SUBSERVICE_NAME);

    private static final DateTimeFormatter YEAR = DateTimeFormatter.ofPattern("uuuu")
        .withZone(ZoneId.from(ZoneOffset.UTC));

    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("MM")
        .withZone(ZoneId.from(ZoneOffset.UTC));

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd")
        .withZone(ZoneId.from(ZoneOffset.UTC));

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private GroovyScriptConfigurationService configurationService;

    @Override
    public AuditRecord createAuditRecord(final RunScriptResponse response) throws GroovyException {
        final AuditRecord auditRecord;

        try (final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(AUTH_INFO)) {
            final Node auditRecordNode = addAuditRecordNode(resourceResolver, response.getUserId());

            setAuditRecordNodeProperties(auditRecordNode, response);

            resourceResolver.commit();

            final Resource auditRecordResource = resourceResolver.getResource(auditRecordNode.getPath());

            auditRecord = new AuditRecordImpl(auditRecordResource);

            LOG.debug("Created audit record: {}", auditRecord);
        } catch (LoginException e) {
            throw new GroovyException("Error authenticating service resource resolver", e);
        } catch (RepositoryException | PersistenceException e) {
            throw new GroovyException("Error creating audit record", e);
        }

        return auditRecord;
    }

    @Override
    public void deleteAllAuditRecords(final String userId) throws GroovyException {
        try (final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(AUTH_INFO)) {

        } catch (LoginException e) {
            throw new GroovyException("Error authenticating service resource resolver", e);
        }
    }

    @Override
    public void deleteAuditRecord(final String userId, final String relativePath) throws GroovyException {
        try (final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(AUTH_INFO)) {

        } catch (LoginException e) {
            throw new GroovyException("Error authenticating service resource resolver", e);
        }
    }

    @Override
    public List<AuditRecord> getAllAuditRecords(final String userId) throws GroovyException {
        final String auditNodePath = getAuditNodePath(userId);

        try (final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(AUTH_INFO)) {
            return findAllAuditRecords(resourceResolver, auditNodePath);
        } catch (LoginException e) {
            throw new GroovyException("Error authenticating service resource resolver", e);
        }
    }

    @Override
    public AuditRecord getAuditRecord(final String userId, final String relativePath) throws GroovyException {
        AuditRecord auditRecord = null;

        try (final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(AUTH_INFO)) {
            final Resource auditRecordResource = resourceResolver.getResource(AUDIT_PATH + "/" + userId)
                .getChild(relativePath);

            if (auditRecordResource != null) {
                auditRecord = new AuditRecordImpl(auditRecordResource);

                LOG.debug("Found audit record: {}", auditRecord);
            }
        } catch (LoginException e) {
            throw new GroovyException("Error authenticating service resource resolver", e);
        }

        return auditRecord;
    }

    @Override
    public List<AuditRecord> getAuditRecords(final String userId, final Calendar startDate,
        final Calendar endDate) throws GroovyException {
        return getAuditRecordsForDateRange(getAllAuditRecords(userId), startDate, endDate);
    }

    @Activate
    public void activate() {
        checkAuditNode();
    }

    private List<AuditRecord> findAllAuditRecords(final ResourceResolver resourceResolver,
        final String auditNodePath) {
        final List<AuditRecord> auditRecords = new ArrayList<>();

        final Resource auditResource = resourceResolver.getResource(auditNodePath);

        if (auditResource != null) {
            final Iterator<Resource> children = auditResource.listChildren();

            while (children.hasNext()) {
                final Resource resource = children.next();

                if (resource.getName().startsWith(AUDIT_RECORD_NODE_PREFIX)) {
                    auditRecords.add(new AuditRecordImpl(resource));
                }

                auditRecords.addAll(findAllAuditRecords(resourceResolver, resource.getPath()));
            }
        }

        return auditRecords;
    }

    private void checkAuditNode() {
        try (final ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(AUTH_INFO)) {
            final Session session = resourceResolver.adaptTo(Session.class);
            final Node consoleRootNode = session.getNode(PATH_GROOVY_ROOT);

            if (!consoleRootNode.hasNode(AUDIT_NODE_NAME)) {
                LOG.info("Audit node does not exist, adding...");

                consoleRootNode.addNode(AUDIT_NODE_NAME, NT_UNSTRUCTURED);

                session.save();
            }
        } catch (LoginException e) {
            LOG.error("Error authenticating service resource resolver", e);
        } catch (RepositoryException e) {
            LOG.error("Error checking audit node", e);
        }
    }

    private void setAuditRecordNodeProperties(final Node auditRecordNode, final RunScriptResponse response)
        throws RepositoryException {
        auditRecordNode.setProperty(SCRIPT, response.getScript());

        if (isNotEmpty(response.getData())) {
            auditRecordNode.setProperty(DATA, response.getData());
        }

        if (isNotEmpty(response.getExceptionStackTrace())) {
            auditRecordNode.setProperty(EXCEPTION_STACK_TRACE, response.getExceptionStackTrace());
        } else {
            if (isNotEmpty(response.getResult())) {
                auditRecordNode.setProperty(RESULT, response.getResult());
            }

            auditRecordNode.setProperty(RUNNING_TIME, response.getRunningTime());
        }

        if (isNotEmpty(response.getOutput())) {
            auditRecordNode.setProperty(OUTPUT, response.getOutput());
        }
    }

    private String getAuditNodePath(final String userId) {
        return configurationService.isDisplayAllAuditRecords() ? AUDIT_PATH : (AUDIT_PATH + "/" + userId);
    }

    private synchronized Node addAuditRecordNode(ResourceResolver resourceResolver, String userId)
        throws RepositoryException {
        final Instant now = Instant.now();

        final String auditRecordParentPath = new StringBuilder()
            .append(AUDIT_PATH).append("/")
            .append(userId).append("/")
            .append(YEAR.format(now)).append("/")
            .append(MONTH.format(now)).append("/")
            .append(DAY.format(now)).append("/")
            .toString();

        final Session session = resourceResolver.adaptTo(Session.class);

        // TODO sling folders
        final Node auditRecordParentNode = JcrUtil.createPath(auditRecordParentPath, NT_UNSTRUCTURED, session);
        final Node auditRecordNode = JcrUtil.createUniqueNode(auditRecordParentNode, AUDIT_RECORD_NODE_PREFIX,
            NT_UNSTRUCTURED, session);

        auditRecordNode.addMixin(MIX_CREATED);

        return auditRecordNode;
    }

    private List<AuditRecord> getAuditRecordsForDateRange(final List<AuditRecord> auditRecords,
        final Calendar startDate, final Calendar endDate) {
        return auditRecords.stream()
            .filter(auditRecord -> {
                final Calendar date = auditRecord.getDate();

                date.set(Calendar.HOUR_OF_DAY, 0);
                date.set(Calendar.MINUTE, 0);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.MILLISECOND, 0);

                return !date.before(startDate) && !date.after(endDate);
            })
            .collect(Collectors.toList());
    }
}
