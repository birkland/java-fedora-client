/*
 * Copyright 2017 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.pass.client.integration;

import static org.dataconservancy.pass.client.fedora.RepositoryCrawler.Ignore.IGNORE_CONTAINERS;
import static org.dataconservancy.pass.client.fedora.RepositoryCrawler.Skip.SKIP_ACLS;
import static org.dataconservancy.pass.client.fedora.RepositoryCrawler.Skip.depth;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.dataconservancy.pass.client.PassClient;
import org.dataconservancy.pass.client.PassClientFactory;
import org.dataconservancy.pass.client.fedora.FedoraConfig;
import org.dataconservancy.pass.client.fedora.RepositoryCrawler;
import org.dataconservancy.pass.model.Submission;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;

/**
 * @author apb@jhu.edu
 */
public class RepositoryCrawlerIT extends ClientITBase {

    final CloseableHttpClient http = getHttpClient();

    // We just want to see if this works with Fedora at all
    @Test
    public void crawlWorksWithFedoraTest() throws Exception {
        final RepositoryCrawler crawler = new RepositoryCrawler();

        final int initialCount = crawler.visit(URI.create(FedoraConfig.getBaseUrl()),
                u -> {
                },
                IGNORE_CONTAINERS,
                depth(2).or(SKIP_ACLS));

        final PassClient client = PassClientFactory.getPassClient();

        final URI submission = client.createResource(new Submission());

        try (CloseableHttpResponse response = http.execute(new HttpPost(submission))) {
            assertNotNull(response.getFirstHeader("Location"));
        }

        final List<URI> found = new ArrayList<>();
        final int afterCount = crawler.visit(URI.create(FedoraConfig.getBaseUrl()), found::add, IGNORE_CONTAINERS,
                depth(2).or(SKIP_ACLS));
        assertEquals(1, afterCount - initialCount);
        assertTrue(found.contains(submission));
    }

}
