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

package org.jetbrains.jet.lang.resolve.java.provider;

import com.intellij.psi.PsiPackage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.resolve.java.PsiClassFinder;

import static org.jetbrains.jet.lang.resolve.java.provider.DeclarationOrigin.JAVA;
import static org.jetbrains.jet.lang.resolve.java.provider.DeclarationOrigin.KOTLIN;

public final class PackagePsiDeclarationProviderImpl extends PsiDeclarationProviderBase implements PackagePsiDeclarationProvider {

    @NotNull
    private final PsiPackage psiPackage;

    @NotNull
    private final PsiClassFinder psiClassFinder;

    /*package private*/ PackagePsiDeclarationProviderImpl(
            @NotNull PsiPackage psiPackage,
            @NotNull PsiClassFinder psiClassFinder
    ) {
        this.psiPackage = psiPackage;
        this.psiClassFinder = psiClassFinder;
    }

    @NotNull
    @Override
    public PsiPackage getPsiPackage() {
        return psiPackage;
    }

    @NotNull
    @Override
    protected MembersCache buildMembersCache() {
        return MembersCache.buildMembersByNameCache(new MembersCache(), psiClassFinder, null, getPsiPackage(), true, getDeclarationOrigin() == KOTLIN);
    }

    @NotNull
    @Override
    public DeclarationOrigin getDeclarationOrigin() {
        return JAVA;
    }
}
