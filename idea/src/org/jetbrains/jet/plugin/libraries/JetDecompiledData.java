/*
 * Copyright 2010-2013 JetBrains s.r.o.
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

package org.jetbrains.jet.plugin.libraries;

import com.beust.jcommander.internal.Maps;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.compiled.ClsElementImpl;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.jet.lang.descriptors.DeclarationDescriptor;
import org.jetbrains.jet.lang.psi.JetDeclaration;
import org.jetbrains.jet.lang.psi.JetFile;

import java.util.Map;

@SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
public class JetDecompiledData {

    private static final Key<JetDecompiledData> USER_DATA_KEY = new Key<JetDecompiledData>("USER_DATA_KEY");
    private static final Object LOCK = new String("decompiled data lock");

    @NotNull
    private final String fileText;
    @NotNull
    private final Map<String, TextRange> renderedDescriptorsToRanges;

    JetDecompiledData(@NotNull String fileText, @NotNull Map<String,TextRange> renderedDescriptorsToRanges) {
        this.fileText = fileText;
        this.renderedDescriptorsToRanges = renderedDescriptorsToRanges;
    }

    @NotNull
    public String getFileText() {
        return fileText;
    }

    @NotNull
    public static JetDecompiledData getDecompiledData(@NotNull VirtualFile virtualFile, @NotNull Project project) {
        synchronized (LOCK) {
            if (virtualFile.getUserData(USER_DATA_KEY) == null) {
                virtualFile.putUserData(USER_DATA_KEY, DecompiledDataFactory.createDecompiledData(virtualFile, project));
            }
            JetDecompiledData decompiledData = virtualFile.getUserData(USER_DATA_KEY);
            assert decompiledData != null;
            return decompiledData;
        }
    }

    @TestOnly
    @NotNull
    public Map<String, JetDeclaration> getRenderedDescriptorToKotlinPsiMap(@NotNull JetFile file) {
        Map<String, JetDeclaration> renderedDescriptorsToJetDeclarations = Maps.newHashMap();
        for (Map.Entry<String, TextRange> renderedDescriptorToRange : renderedDescriptorsToRanges.entrySet()) {
            String renderedDescriptor = renderedDescriptorToRange.getKey();
            TextRange range = renderedDescriptorToRange.getValue();
            JetDeclaration jetDeclaration = PsiTreeUtil.findElementOfClassAtRange(file, range.getStartOffset(), range.getEndOffset(),
                                                                                  JetDeclaration.class);
            assert jetDeclaration != null : "Can't find declaration at " + range + ": "
                                            + file.getText().substring(range.getStartOffset(), range.getEndOffset());
            renderedDescriptorsToJetDeclarations.put(renderedDescriptor, jetDeclaration);
        }
        return renderedDescriptorsToJetDeclarations;
    }

    @Nullable
    public JetDeclaration getDeclarationForDescriptor(@NotNull JetFile file, @NotNull DeclarationDescriptor descriptor) {
        String key = DecompiledDataFactory.DESCRIPTOR_RENDERER.render(descriptor);
        return getRenderedDescriptorToKotlinPsiMap(file).get(key);
    }
}
