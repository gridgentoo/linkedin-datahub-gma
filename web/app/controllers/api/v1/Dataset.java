/**
 * Copyright 2015 LinkedIn Corp. All rights reserved.
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
 */
package controllers.api.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.linkedin.dataset.SchemaFieldArray;
import dao.MetadataStoreDao;
import dao.ReturnCode;
import models.DatasetColumn;
import models.DatasetCompliance;
import models.DatasetDependency;
import models.DatasetSecurity;
import models.ImpactDataset;
import org.apache.commons.lang3.math.NumberUtils;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.Logger;
import org.apache.commons.lang3.StringUtils;
import dao.DatasetsDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Dataset extends Controller
{
    public static Result getDatasetOwnerTypes()
    {
        ObjectNode result = Json.newObject();

        result.put("status", "ok");
        result.set("ownerTypes", Json.toJson(DatasetsDAO.getDatasetOwnerTypes()));
        return ok(result);
    }

    public static Result getPagedDatasets()
    {
        ObjectNode result = Json.newObject();
        String urn = request().getQueryString("urn");

        int page = 1;
        String pageStr = request().getQueryString("page");
        if (StringUtils.isBlank(pageStr))
        {
            page = 1;
        }
        else
        {
            try
            {
                page = Integer.parseInt(pageStr);
            }
            catch(NumberFormatException e)
            {
                Logger.error("Dataset Controller getPagedDatasets wrong page parameter. Error message: " + e.getMessage());
                page = 1;
            }
        }

        int size = 10;
        String sizeStr = request().getQueryString("size");
        if (StringUtils.isBlank(sizeStr))
        {
            size = 10;
        }
        else
        {
            try
            {
                size = Integer.parseInt(sizeStr);
            }
            catch(NumberFormatException e)
            {
                Logger.error("Dataset Controller getPagedDatasets wrong size parameter. Error message: " + e.getMessage());
                size = 10;
            }
        }

        result.put("status", "ok");
        String username = session("user");
        result.set("data", DatasetsDAO.getPagedDatasets(urn, page, size, username));
        return ok(result);
    }

    public static Result getDatasetByID(int id)
    {
        String username = session("user");
        models.Dataset dataset = DatasetsDAO.getDatasetByID(id, username);

        ObjectNode result = Json.newObject();

        if (dataset != null)
        {
            result.put("status", "ok");
            result.set("dataset", Json.toJson(dataset));
        }
        else
        {
            result.put("status", "error");
            result.put("message", "record not found");
        }

        return ok(result);
    }

    public static Result getDatasetColumnByID(int datasetId, int columnId)
    {
        List<DatasetColumn> datasetColumnList = DatasetsDAO.getDatasetColumnByID(datasetId, columnId);

        ObjectNode result = Json.newObject();

        if (datasetColumnList != null && datasetColumnList.size() > 0)
        {
            result.put("status", "ok");
            result.set("columns", Json.toJson(datasetColumnList));
        }
        else
        {
            result.put("status", "error");
            result.put("message", "record not found");
        }

        return ok(result);
    }

    public static Result getDatasetColumnsByID(int id) {
        ObjectNode result = Json.newObject();

        String urn = DatasetsDAO.getDatasetUrnById(id);
        if (urn == null || urn.length() < 6) {
            Logger.debug("Dataset id " + id + " not found.");
            result.put("status", "error");
            result.put("message", "Dataset id not found");
            return ok(result);
        }

        // try to access dataset fields from Metadata Store schemaMetadata first
        SchemaFieldArray datasetFields = null;
        try {
            datasetFields = MetadataStoreDao.getLatestSchemaByWhUrn(urn).getFields();
        } catch (Exception e) {
            Logger.debug("Can't find schema for URN: " + urn + ", Exception: " + e.getMessage());
        }

        List<DatasetColumn> datasetColumnList;
        if (datasetFields != null && datasetFields.size() > 0) {
            datasetColumnList = MetadataStoreDao.datasetColumnsMapper(datasetFields);
        } else { // get fields from WH db if nothing in Metadata store
            datasetColumnList = DatasetsDAO.getDatasetColumnsByID(id);
        }

        if (datasetColumnList != null && datasetColumnList.size() > 0) {
            result.put("status", "ok");
            result.set("columns", Json.toJson(datasetColumnList));
        } else {
            result.put("status", "error");
            result.put("message", "record not found");
        }
        return ok(result);
    }

    public static Result getDatasetPropertiesByID(int id)
    {
        JsonNode properties = DatasetsDAO.getDatasetPropertiesByID(id);

        ObjectNode result = Json.newObject();

        if (properties != null)
        {
            result.put("status", "ok");
            result.set("properties", properties);
        }
        else
        {
            result.put("status", "error");
            result.put("message", "record not found");
        }

        return ok(result);
    }

    public static Result getDatasetSampleDataByID(int id)
    {
        JsonNode sampleData = DatasetsDAO.getDatasetSampleDataByID(id);

        ObjectNode result = Json.newObject();

        if (sampleData != null)
        {
            result.put("status", "ok");
            result.set("sampleData", sampleData);
        }
        else
        {
            result.put("status", "error");
            result.put("message", "record not found");
        }

        return ok(result);
    }

    public static Result getDatasetOwnersByID(int id)
    {
        ObjectNode result = Json.newObject();

        result.put("status", "ok");
        result.set("owners", Json.toJson(DatasetsDAO.getDatasetOwnersByID(id)));

        return ok(result);
    }

    public static Result getDatasetImpactAnalysisByID(int id)
    {
        List<ImpactDataset> impactDatasetList = DatasetsDAO.getImpactAnalysisByID(id);

        ObjectNode result = Json.newObject();

        if (impactDatasetList != null)
        {
            result.put("status", "ok");
            result.set("impacts", Json.toJson(impactDatasetList));
        }
        else
        {
            result.put("status", "error");
            result.put("message", "record not found");
        }

        return ok(result);
    }

    public static Result updateDatasetOwners(int id) {
        ObjectNode result = Json.newObject();
        String username = session("user");
        Map<String, String[]> params = request().body().asFormUrlEncoded();

        if (StringUtils.isNotBlank(username)) {
            ReturnCode code = DatasetsDAO.updateDatasetOwners(id, params, username);

            if (code == ReturnCode.Success) {
                result.put("status", "success");
            } else if (code == ReturnCode.RuleViolation) {
                result.put("status", "failed");
                result.put("error", "true");
                result.put("msg", "Less than 2 confirmed owners.");
            } else {
                result.put("status", "failed");
                result.put("error", "true");
                result.put("msg", "Could not update dataset owners.");
            }
        } else {
            result.put("status", "failed");
            result.put("error", "true");
            result.put("msg", "Unauthorized User.");
        }
        return ok(result);
    }

    public static Result favoriteDataset(int id)
    {
        ObjectNode result = Json.newObject();
        String username = session("user");
        if (StringUtils.isNotBlank(username))
        {
            if (DatasetsDAO.favorite(id, username))
            {
                result.put("status", "success");
            }
            else
            {
                result.put("status", "failed");
            }
        }
        else
        {
            result.put("status", "failed");
        }

        return ok(result);
    }

    public static Result unfavoriteDataset(int id)
    {
        ObjectNode result = Json.newObject();
        String username = session("user");
        if (StringUtils.isNotBlank(username))
        {
            if (DatasetsDAO.unfavorite(id, username))
            {
                result.put("status", "success");
            }
            else
            {
                result.put("status", "failed");
            }
        }
        else
        {
            result.put("status", "failed");
        }

        return ok(result);
    }

    public static Result getDatasetOwnedBy(String userId) {
        ObjectNode result = Json.newObject();
        int page = NumberUtils.toInt(request().getQueryString("page"), 1);
        int size = NumberUtils.toInt(request().getQueryString("size"), 10);

        if (StringUtils.isNotBlank(userId)) {
            result = DatasetsDAO.getDatasetOwnedBy(userId, page, size);
        } else {
            result.put("status", "failed, no user");
        }
        return ok(result);
    }

    public static Result ownDataset(int id)
    {
        ObjectNode result = Json.newObject();
        String username = session("user");
        if (StringUtils.isNotBlank(username))
        {
            result = DatasetsDAO.ownDataset(id, username);
        }
        else
        {
            result.put("status", "failed");
        }

        return ok(result);
    }

    public static Result unownDataset(int id)
    {
        ObjectNode result = Json.newObject();
        String username = session("user");
        if (StringUtils.isNotBlank(username))
        {
            result = DatasetsDAO.unownDataset(id, username);
        }
        else
        {
            result.put("status", "failed");
        }

        return ok(result);
    }

    public static Result getFavorites()
    {
        ObjectNode result = Json.newObject();
        String username = session("user");
        result.put("status", "ok");
        result.set("data", DatasetsDAO.getFavorites(username));
        return ok(result);
    }

    public static Result getPagedDatasetComments(int id)
    {
        ObjectNode result = Json.newObject();
        String username = session("user");
        if (username == null)
        {
            username = "";
        }

        int page = 1;
        String pageStr = request().getQueryString("page");
        if (StringUtils.isBlank(pageStr))
        {
            page = 1;
        }
        else
        {
            try
            {
                page = Integer.parseInt(pageStr);
            }
            catch(NumberFormatException e)
            {
                Logger.error("Dataset Controller getPagedDatasetComments wrong page parameter. Error message: " +
                    e.getMessage());
                page = 1;
            }
        }

        int size = 10;
        String sizeStr = request().getQueryString("size");
        if (StringUtils.isBlank(sizeStr))
        {
            size = 10;
        }
        else
        {
            try
            {
                size = Integer.parseInt(sizeStr);
            }
            catch(NumberFormatException e)
            {
                Logger.error("Dataset Controller getPagedDatasetComments wrong size parameter. Error message: " +
                    e.getMessage());
                size = 10;
            }
        }

        result.put("status", "ok");
        result.set("data", DatasetsDAO.getPagedDatasetComments(username, id, page, size));
        return ok(result);
    }

    public static Result postDatasetComment(int id)
    {
        String body = request().body().asText();
        ObjectNode result = Json.newObject();
        String username = session("user");
        Map<String, String[]> params = request().body().asFormUrlEncoded();

        if (StringUtils.isNotBlank(username))
        {
            if (DatasetsDAO.postComment(id, params, username))
            {
                result.put("status", "success");
            }
            else
            {
                result.put("status", "failed");
                result.put("error", "true");
                result.put("msg", "Could not create comment.");
                return badRequest(result);
            }
        }
        else
        {
            result.put("status", "failed");
            result.put("error", "true");
            result.put("msg", "Unauthorized User.");
            return badRequest(result);
        }

        return ok(result);
    }

    public static Result putDatasetComment(int id, int commentId)
    {
        String body = request().body().asText();
        ObjectNode result = Json.newObject();
        String username = session("user");
        Map<String, String[]> params = request().body().asFormUrlEncoded();

        if (StringUtils.isNotBlank(username))
        {
            if (DatasetsDAO.postComment(id, params, username))
            {
                result.put("status", "success");
                return ok(result);
            }
            else
            {
                result.put("status", "failed");
                result.put("error", "true");
                result.put("msg", "Could not create comment.");
                return badRequest(result);
            }
        }
        else
        {
            result.put("status", "failed");
            result.put("error", "true");
            result.put("msg", "Unauthorized User");
            return badRequest(result);
        }

    }

    public static Result deleteDatasetComment(int id, int commentId)
    {
        ObjectNode result = Json.newObject();
        if (DatasetsDAO.deleteComment(commentId))
        {
            result.put("status", "success");
        }
        else
        {
            result.put("status", "failed");
        }

        return ok(result);
    }

    public static Result watchDataset(int id)
    {
        ObjectNode result = Json.newObject();
        String username = session("user");
        Map<String, String[]> params = request().body().asFormUrlEncoded();
        if (StringUtils.isNotBlank(username))
        {
            String message = DatasetsDAO.watchDataset(id, params, username);
            if (StringUtils.isBlank(message))
            {
                result.put("status", "success");
            }
            else
            {
                result.put("status", "failed");
                result.put("message", message);
            }
        }
        else
        {
            result.put("status", "failed");
            result.put("message", "User is not authenticated");
        }

        return ok(result);
    }

    public static Result getWatchedUrnId()
    {
        ObjectNode result = Json.newObject();
        String urn = request().getQueryString("urn");
        result.put("status", "success");
        Long id = 0L;

        if (StringUtils.isNotBlank(urn))
        {
            String username = session("user");
            if (StringUtils.isNotBlank(username))
            {
                id = DatasetsDAO.getWatchId(urn, username);
            }
        }
        result.put("id", id);

        return ok(result);
    }

    public static Result watchURN()
    {
        Map<String, String[]> params = request().body().asFormUrlEncoded();
        ObjectNode result = Json.newObject();

        String username = session("user");
        if (StringUtils.isNotBlank(username))
        {
            String message = DatasetsDAO.watchURN(params, username);
            if (StringUtils.isBlank(message))
            {
                result.put("status", "success");
            }
            else
            {
                result.put("status", "failed");
                result.put("message", message);
            }
        }
        else
        {
            result.put("status", "failed");
            result.put("message", "User is not authenticated");
        }
        return ok(result);
    }

    public static Result unwatchDataset(int id, int watchId)
    {
        ObjectNode result = Json.newObject();
        if (DatasetsDAO.unwatch(watchId))
        {
            result.put("status", "success");
        }
        else
        {
            result.put("status", "failed");
        }

        return ok(result);
    }

    public static Result unwatchURN(int watchId)
    {
        ObjectNode result = Json.newObject();
        if (DatasetsDAO.unwatch(watchId))
        {
            result.put("status", "success");
        }
        else
        {
            result.put("status", "failed");
        }

        return ok(result);
    }

    public static Result getPagedDatasetColumnComments(int datasetId, int columnId)
    {
        ObjectNode result = Json.newObject();

        String username = session("user");
        if (username == null)
        {
            username = "";
        }

        int page = 1;
        String pageStr = request().getQueryString("page");
        if (StringUtils.isBlank(pageStr))
        {
            page = 1;
        }
        else
        {
            try
            {
                page = Integer.parseInt(pageStr);
            }
            catch(NumberFormatException e)
            {
                Logger.error("Dataset Controller getPagedDatasetColumnComments wrong page parameter. Error message: " +
                    e.getMessage());
                page = 1;
            }
        }

        int size = 10;
        String sizeStr = request().getQueryString("size");
        if (StringUtils.isBlank(sizeStr))
        {
            size = 10;
        }
        else
        {
            try
            {
                size = Integer.parseInt(sizeStr);
            }
            catch(NumberFormatException e)
            {
                Logger.error("Dataset Controller getPagedDatasetColumnComments wrong size parameter. Error message: " +
                    e.getMessage());
                size = 10;
            }
        }

        result.put("status", "ok");
        result.set("data", DatasetsDAO.getPagedDatasetColumnComments(username, datasetId, columnId, page, size));
        return ok(result);
    }

    public static Result postDatasetColumnComment(int id, int columnId)
    {
        ObjectNode result = Json.newObject();
        String username = session("user");
        Map<String, String[]> params = request().body().asFormUrlEncoded();
        if (StringUtils.isNotBlank(username))
        {
            String errorMsg = DatasetsDAO.postColumnComment(id, columnId, params, username);
            if (StringUtils.isBlank(errorMsg))
            {
                result.put("status", "success");
            }
            else
            {
                result.put("status", "failed");
                result.put("msg", errorMsg);
            }
        }
        else
        {
            result.put("status", "failed");
            result.put("msg", "Authentication Required");
        }

        return ok(result);
    }

    public static Result putDatasetColumnComment(int id, int columnId, int commentId)
    {
        ObjectNode result = Json.newObject();
        String username = session("user");
        Map<String, String[]> params = request().body().asFormUrlEncoded();
        if (StringUtils.isNotBlank(username))
        {
            String errorMsg = DatasetsDAO.postColumnComment(id, commentId, params, username);
            if (StringUtils.isBlank(errorMsg))
            {
                result.put("status", "success");
            }
            else
            {
                result.put("status", "failed");
                result.put("msg", errorMsg);
            }
        }
        else
        {
            result.put("status", "failed");
            result.put("msg", "Authentication Required");
        }

        return ok(result);
    }

    public static Result assignCommentToColumn(int datasetId, int columnId)
    {
        ObjectNode json = Json.newObject();
        ArrayNode res = json.arrayNode();
        JsonNode req = request().body().asJson();
        if (req == null) {
            return badRequest("Expecting JSON data");
        }
        if (req.isArray()) {
            for (int i = 0; i < req.size(); i++) {
                JsonNode obj = req.get(i);
                Boolean isSuccess = DatasetsDAO.assignColumnComment(
                    obj.get("datasetId").asInt(),
                    obj.get("columnId").asInt(),
                    obj.get("commentId").asInt());
                ObjectNode itemResponse = Json.newObject();
                if (isSuccess) {
                    itemResponse.put("success", "true");
                } else {
                    itemResponse.put("error", "true");
                    itemResponse.put("datasetId", datasetId);
                    itemResponse.put("columnId", columnId);
                    itemResponse.set("commentId", obj.get("comment_id"));
                }
                res.add(itemResponse);
            }
        } else {
            Boolean isSuccess = DatasetsDAO.assignColumnComment(
                datasetId,
                columnId,
                req.get("commentId").asInt());
            ObjectNode itemResponse = Json.newObject();
            if (isSuccess) {
                itemResponse.put("success", "true");
            } else {
                itemResponse.put("error", "true");
                itemResponse.put("datasetId", datasetId);
                itemResponse.put("columnId", columnId);
                itemResponse.set("commentId", req.get("commentId"));
            }
            res.add(itemResponse);
        }
        ObjectNode result = Json.newObject();
        result.putArray("results").addAll(res);
        return ok(result);
    }

    public static Result deleteDatasetColumnComment(int id, int columnId, int commentId)
    {
        ObjectNode result = Json.newObject();
        if (DatasetsDAO.deleteColumnComment(id, columnId, commentId))
        {
            result.put("status", "success");
        }
        else
        {
            result.put("status", "failed");
        }

        return ok(result);
    }

    public static Result getSimilarColumnComments(Long datasetId, int columnId) {
        ObjectNode result = Json.newObject();
        result.set("similar", Json.toJson(DatasetsDAO.similarColumnComments(datasetId, columnId)));
        return ok(result);
    }

    public static Result getSimilarColumns(int datasetId, int columnId)
    {
        ObjectNode result = Json.newObject();
        result.set("similar", Json.toJson(DatasetsDAO.similarColumns(datasetId, columnId)));
        return ok(result);
    }

    public static Result getDependViews(Long datasetId)
    {
        ObjectNode result = Json.newObject();
        List<DatasetDependency> depends = new ArrayList<>();
        DatasetsDAO.getDependencies(datasetId, depends);
        result.put("status", "ok");
        result.set("depends", Json.toJson(depends));
        return ok(result);
    }

    public static Result getReferenceViews(Long datasetId)
    {
        ObjectNode result = Json.newObject();
        List<DatasetDependency> references = new ArrayList<>();
        DatasetsDAO.getReferences(datasetId, references);
        result.put("status", "ok");
        result.set("references", Json.toJson(references));
        return ok(result);
    }

    public static Result getDatasetListNodes()
    {
        ObjectNode result = Json.newObject();
        String urn = request().getQueryString("urn");
        result.put("status", "ok");
        result.set("nodes", Json.toJson(DatasetsDAO.getDatasetListViewNodes(urn)));
        return ok(result);
    }

    public static Result getDatasetVersions(Long datasetId, Integer dbId)
    {
        ObjectNode result = Json.newObject();
        result.put("status", "ok");
        result.set("versions", Json.toJson(DatasetsDAO.getDatasetVersions(datasetId, dbId)));
        return ok(result);
    }

    public static Result getDatasetSchemaTextByVersion(Long datasetId, String version)
    {
        ObjectNode result = Json.newObject();
        result.put("status", "ok");
        result.set("schema_text", Json.toJson(DatasetsDAO.getDatasetSchemaTextByVersion(datasetId, version)));
        return ok(result);
    }

    public static Result getDatasetInstances(Long datasetId)
    {
        ObjectNode result = Json.newObject();
        result.put("status", "ok");
        result.set("instances", Json.toJson(DatasetsDAO.getDatasetInstances(datasetId)));
        return ok(result);
    }

    public static Result getDatasetPartitions(Long datasetId)
    {
        ObjectNode result = Json.newObject();
        result.put("status", "ok");
        result.set("partitions", Json.toJson(DatasetsDAO.getDatasetPartitionGains(datasetId)));
        return ok(result);
    }

    public static Result getDatasetAccess(Long datasetId)
    {
        ObjectNode result = Json.newObject();
        result.put("status", "ok");
        result.set("access", Json.toJson(DatasetsDAO.getDatasetAccessibilty(datasetId)));
        return ok(result);
    }

    public static Promise<Result> getDatasetCompliance(int datasetId) {
        DatasetCompliance record = null;
        try {
            record = DatasetsDAO.getDatasetComplianceByDatasetId(datasetId);
        } catch (Exception e) {
            JsonNode result = Json.newObject()
                .put("status", "failed")
                .put("error", "true")
                .put("msg", "Fetch data Error: " + e.getMessage());

            return Promise.promise(() -> ok(result));
        }

        JsonNode result = Json.newObject().put("status", "ok")
            .set("privacyCompliancePolicy", Json.toJson(record));

        return Promise.promise(() -> ok(result));
    }

    public static Promise<Result> updateDatasetCompliance(int datasetId) {
        String username = session("user");
        if (StringUtils.isBlank(username)) {
            JsonNode result =
                Json.newObject().put("status", "failed")
                    .put("error", "true")
                    .put("msg", "Unauthorized User.");

            return Promise.promise(() -> ok(result));
        }

        try {
            DatasetsDAO.updateDatasetCompliancePolicy(datasetId, request().body().asJson());
        } catch (Exception e) {
            JsonNode result = Json.newObject()
                .put("status", "failed")
                .put("error", "true")
                .put("msg", "Update Error: " + e.getMessage());

            Logger.warn("Update fail", e);

            return Promise.promise(() -> ok(result));
        }
        return Promise.promise(() -> ok(Json.newObject().put("status", "ok")));
    }

    public static Promise<Result> getDatasetSecurity(int datasetId) {
        DatasetSecurity record = null;
        try {
            record = DatasetsDAO.getDatasetSecurityByDatasetId(datasetId);
        } catch (Exception e) {
            JsonNode result = Json.newObject()
                .put("status", "failed")
                .put("error", "true")
                .put("msg", "Fetch data Error: " + e.getMessage());

            return Promise.promise(() -> ok(result));
        }

        JsonNode result = Json.newObject().put("status", "ok")
            .set("securitySpecification", Json.toJson(record));

        return Promise.promise(() -> ok(result));
    }

    public static Promise<Result> updateDatasetSecurity(int datasetId) {
        String username = session("user");
        if (StringUtils.isBlank(username)) {
            JsonNode result =
                Json.newObject().put("status", "failed")
                    .put("error", "true")
                    .put("msg", "Unauthorized User.");

            return Promise.promise(() -> ok(result));
        }

        try {
            DatasetsDAO.updateDatasetSecurityInfo(datasetId, request().body().asJson());
        } catch (Exception e) {
            JsonNode result = Json.newObject()
                .put("status", "failed")
                .put("error", "true")
                .put("msg", "Update Error: " + e.getMessage());

            Logger.warn("Update fail", e);

            return Promise.promise(() -> ok(result));
        }
        return Promise.promise(() -> ok(Json.newObject().put("status", "ok")));
    }
}
