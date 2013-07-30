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

import com.google.common.collect.Sets;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.psi.JetFqNamedDeclaration;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.plugin.stubindex.JetTopLevelExtensionFunctionShortNameIndex;
import org.jetbrains.jet.plugin.stubindex.JetTopLevelNonExtensionFunctionShortNameIndex;
import org.jetbrains.jet.plugin.stubindex.JetTopLevelObjectShortNameIndex;

import java.util.Collection;
import java.util.Set;

public final class TopLevelDeclarationIndexUtils {
    @NotNull
    public static Collection<FqName> getExtensionFunctionFqNames(
            @NotNull Condition<String> acceptedShortNameCondition,
            @NotNull GlobalSearchScope searchScope
    ) {
        return getAllTopLevelEntitiesFqNamesByNameConditionFromIndex(acceptedShortNameCondition, searchScope,
                                                                     JetTopLevelExtensionFunctionShortNameIndex.getInstance());
    }

    @NotNull
    public static Collection<FqName> getTopLevelNonExtensionFunctionFqNames(
            @NotNull Condition<String> acceptedShortNameCondition,
            @NotNull GlobalSearchScope searchScope
    ) {
        return getAllTopLevelEntitiesFqNamesByNameConditionFromIndex(acceptedShortNameCondition, searchScope,
                                                                     JetTopLevelNonExtensionFunctionShortNameIndex.getInstance());
    }

    @NotNull
    public static Collection<FqName> getTopLevelObjectFqNames(
            @NotNull Condition<String> acceptedShortNameCondition,
            @NotNull GlobalSearchScope searchScope
    ) {
        return getAllTopLevelEntitiesFqNamesByNameConditionFromIndex(acceptedShortNameCondition, searchScope,
                                                                     JetTopLevelObjectShortNameIndex.getInstance());
    }

    @NotNull
    private static <T extends JetFqNamedDeclaration> Collection<FqName> getAllTopLevelEntitiesFqNamesByNameConditionFromIndex(
            @NotNull Condition<String> acceptedShortNameCondition,
            @NotNull GlobalSearchScope searchScope,
            @NotNull StringStubIndexExtension<T> index
    ) {
        Collection<String> allShortNames = index.getAllKeys(searchScope.getProject());
        Collection<String> acceptedShortNames = ContainerUtil.filter(allShortNames, acceptedShortNameCondition);
        Set<FqName> result = Sets.newHashSet();
        for (String name : acceptedShortNames) {
            Collection<T> entities = index.get(name, searchScope.getProject(), searchScope);
            for (T entity : entities) {
                ContainerUtil.addIfNotNull(result, entity.getFqName());
            }
        }
        return result;
    }

    private TopLevelDeclarationIndexUtils() {
    }
}
