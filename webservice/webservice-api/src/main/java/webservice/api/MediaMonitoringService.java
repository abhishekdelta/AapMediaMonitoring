package webservice.api;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.aap.monitoring.Article;
import org.aap.monitoring.ArticleCount;

@Path( "media" )
public interface MediaMonitoringService
{
    @Path( "getArticles" )
    @GET
    @Produces( { MediaType.APPLICATION_JSON } )
    Collection<Article> getArticles(@QueryParam(value = "query") String keyword, @QueryParam(value = "startDate") long startDate, @QueryParam(value = "endDate") long endDate, @QueryParam(value = "src") String src);
    
    @Path("getArticlesCount")
    @GET
    @Produces( { MediaType.APPLICATION_JSON } )
    ArticleCount getNumArticles(@QueryParam(value = "query") String keyword, @QueryParam(value = "startDate") long startDate, @QueryParam(value = "endDate") long endDate, @QueryParam(value = "src") String src);
    
    @Path("triggerIndexer")
    @GET
    @Produces( { MediaType.APPLICATION_JSON } )
    //yyyy-MM-dd
    Response triggerIndexer(@QueryParam(value = "date")String date);
    
}
