package com.adobe.acs.commons.groovy.audit;

import java.util.Calendar;
import java.util.List;

import javax.annotation.Nullable;

import com.adobe.acs.commons.groovy.audit.AuditRecord;
import com.adobe.acs.commons.groovy.exception.GroovyException;
import com.adobe.acs.commons.groovy.response.RunScriptResponse;

public interface GroovyScriptAuditService {

    /**
     * Create an audit record for the given script execution response.
     *
     * @param response response containing execution result or exception
     */
    AuditRecord createAuditRecord(RunScriptResponse response) throws GroovyException;

    /**
     * Delete all audit records.
     *
     * @param userId user that owns the audit records
     */
    void deleteAllAuditRecords(String userId) throws GroovyException;

    /**
     * Delete an audit record.
     *
     * @param userId user that owns the audit record
     * @param relativePath relative path to audit record from parent audit resource
     */
    void deleteAuditRecord(String userId, String relativePath) throws GroovyException;

    /**
     * Get all audit records.
     *
     * @param userId user that owns the audit records
     * @return all audit records
     */
    List<AuditRecord> getAllAuditRecords(String userId) throws GroovyException;

    /**
     * Get the audit record at the given relative path.
     *
     * @param userId user that owns the audit record
     * @param relativePath relative path to audit record from parent audit node
     * @return audit record or null if none exists
     */
    @Nullable
    AuditRecord getAuditRecord(String userId, String relativePath) throws GroovyException;

    /**
     * Get a list of audit records for the given date range.
     *
     * @param userId user that owns the audit records
     * @param startDate start date
     * @param endDate end date
     * @return list of audit records in the given date range
     */
    List<AuditRecord> getAuditRecords(String userId, Calendar startDate, Calendar endDate) throws GroovyException;
}
