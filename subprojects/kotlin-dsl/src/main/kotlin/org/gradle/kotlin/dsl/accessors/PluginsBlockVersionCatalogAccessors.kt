/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.kotlin.dsl.accessors

import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.codegen.kotlinDslPackagePath
import org.gradle.kotlin.dsl.concurrent.IO
import org.gradle.plugin.use.PluginDependency
import java.io.File


data class PluginsBlockVersionCatalogInput(
    val pluginDependenciesByCatalog: Map<String, List<PluginDependency>>
)

internal
fun buildPluginsBlockVersionCatalogInputFor(versionCatalogs: VersionCatalogsExtension?): PluginsBlockVersionCatalogInput? {
    if (versionCatalogs == null) return null
    return PluginsBlockVersionCatalogInput(
        versionCatalogs
            .groupBy { it.name }
            .mapValues { (_, catalogs) ->
                catalogs.flatMap { catalog ->
                    catalog.pluginAliases.mapNotNull { catalog.findPlugin(it).orElse(null).get() }
                }
            }
    )
}

internal
fun IO.buildPluginsBlockVersionCatalogAccessorsFor(
    versionCatalogs: VersionCatalogsExtension,
    srcDir: File,
    binDir: File

) {
    makeAccessorOutputDirs(srcDir, binDir, kotlinDslPackagePath)

    versionCatalogs.map { catalog ->
        catalog.name
        catalog.pluginAliases
    }

}
