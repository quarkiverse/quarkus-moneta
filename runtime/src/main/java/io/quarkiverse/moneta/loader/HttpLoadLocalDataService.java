/*
  Copyright (c) 2012, 2024, Werner Keil and others by the @author tag.

  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy of
  the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  License for the specific language governing permissions and limitations under
  the License.
 */
package io.quarkiverse.moneta.loader;

import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.javamoney.moneta.spi.loader.LoaderListener;

class HttpLoadLocalDataService {

    private static final Logger LOG = Logger.getLogger(HttpLoadLocalDataService.class.getName());

    private final Map<String, LoadableHttpResource> resources;

    private final LoaderListener listener;

    HttpLoadLocalDataService(Map<String, LoadableHttpResource> resources,
            LoaderListener listener) {
        this.resources = resources;
        this.listener = listener;
    }

    public boolean execute(String resourceId) {
        LoadableHttpResource load = this.resources.get(resourceId);
        if (Objects.nonNull(load)) {
            try {
                if (load.loadFallback()) {
                    listener.trigger(resourceId, load);
                    return true;
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to load resource locally: " + resourceId, e);
            }
        } else {
            throw new IllegalArgumentException("No such resource: " + resourceId);
        }
        return false;
    }

    @Override
    public String toString() {
        return HttpLoadLocalDataService.class.getName() + '{' + " resources: "
                + resources + ", defaultLoaderListener: " + listener + '}';
    }
}
