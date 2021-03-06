/*
 * Copyright 2010-2012 JetBrains s.r.o.
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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.maven;

import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.cli.common.CompilerPlugin;
import org.jetbrains.jet.cli.common.CompilerPluginContext;
import org.jetbrains.jet.internal.com.intellij.openapi.project.Project;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.k2js.config.Config;
import org.jetbrains.k2js.config.EcmaVersion;
import org.jetbrains.k2js.facade.K2JSTranslator;
import org.jetbrains.k2js.facade.MainCallParameters;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Compiles Kotlin code to JavaScript
 */
public class K2JSCompilerPlugin implements CompilerPlugin {
    public static final String KOTLIN_JS_LIB = "kotlin-lib.js";
    private String outFile = "target/js/program.js";

    public K2JSCompilerPlugin() {
    }

    @Override
    public void processFiles(@NotNull CompilerPluginContext context) {
        Project project = context.getProject();
        BindingContext bindingContext = context.getContext();
        List<JetFile> sources = context.getFiles();

        if (bindingContext != null && sources != null && project != null) {
            Config config = new JsLibrarySourceConfig(project, EcmaVersion.defaultVersion());

            // lets copy the kotlin library into the output directory
            try {
                File parentFile = new File(outFile).getParentFile();
                parentFile.mkdirs();
                final InputStream inputStream = JsLibrarySourceConfig.loadClasspathResource(KOTLIN_JS_LIB);
                if (inputStream == null) {
                    System.out.println("WARNING: Could not find " + KOTLIN_JS_LIB + " on the classpath!");
                } else {
                    Files.copy(new InputSupplier<InputStream>() {
                        @Override
                        public InputStream getInput() throws IOException {
                            return inputStream;
                        }
                    }, new File(parentFile, "kotlin-lib.js"));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                K2JSTranslator translator = new K2JSTranslator(config);

                final String code = translator.generateProgramCode(sources, MainCallParameters.noCall());

                File file = new File(outFile);
                Files.createParentDirs(file);
                Files.write(code, file, Charset.forName("UTF-8"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setOutFile(String outFile) {
        this.outFile = outFile;
    }
}
