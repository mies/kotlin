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

package org.jetbrains.jet.plugin.caches;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.descriptors.serialization.ClassData;
import org.jetbrains.jet.descriptors.serialization.DescriptorDeserializer;
import org.jetbrains.jet.descriptors.serialization.Flags;
import org.jetbrains.jet.lang.descriptors.ClassKind;
import org.jetbrains.jet.lang.resolve.java.resolver.KotlinClassFileHeader;

/**
 * Number of helper methods for searching jet element prototypes in java. Methods use java indices for search.
 */
public class JetFromJavaDescriptorHelper {

    private JetFromJavaDescriptorHelper() {
    }

    @Nullable
    public static ClassKind getCompiledClassKind(@NotNull PsiClass psiClass) {
        ClassData classData = getClassData(psiClass);
        if (classData == null) return null;
        return DescriptorDeserializer.classKind(Flags.CLASS_KIND.get(classData.getClassProto().getFlags()));
    }


    @Nullable
    private static ClassData getClassData(@NotNull PsiClass psiClass) {
        VirtualFile virtualFile = getVirtualFileForPsiClass(psiClass);
        if (virtualFile == null) return null;
        return KotlinClassFileHeader.readKotlinHeaderFromClassFile(virtualFile).readClassData();
    }

    //TODO: common utility
    //TODO: doesn't work for inner classes and stuff
    @Nullable
    private static VirtualFile getVirtualFileForPsiClass(@NotNull PsiClass psiClass) {
        PsiFile psiFile = psiClass.getContainingFile();
        if (psiFile == null) {
            return null;
        }
        VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null) {
            return null;
        }
        return virtualFile;
    }
}
