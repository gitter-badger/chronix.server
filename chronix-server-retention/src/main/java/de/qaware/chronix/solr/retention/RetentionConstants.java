/*
 * Copyright (C) 2015 QAware GmbH
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package de.qaware.chronix.solr.retention;

/**
 * Constants for the retention query handler
 *
 * @author f.lautenschlager
 */
public class RetentionConstants {

    public static final String QUERY_FIELD = "queryField";
    public static final String REMOVE_TIME_SERIES_OLDER = "timeSeriesAge";
    public static final String OPTIMIZE_AFTER_DELETION = "optimizeAfterDeletion";
    public static final String SOFT_COMMIT = "softCommit";
    public static final String REMOVE_DAILY_AT = "removeDailyAt";
    public static final String RETENTION_URL = "retentionUrl";

    /**
     * Private constructor to avoid instances
     */
    private RetentionConstants() {

    }
}
