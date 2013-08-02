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

package org.jetbrains.jet.lang.resolve.java.structure;

import com.intellij.psi.PsiArrayType;
import org.jetbrains.annotations.NotNull;

public class JavaArrayType extends JavaType {
    public JavaArrayType(@NotNull PsiArrayType psiArrayType) {
        super(psiArrayType);
    }

    @NotNull
    @Override
    public PsiArrayType getPsi() {
        return (PsiArrayType) super.getPsi();
    }

    @NotNull
    public static JavaArrayType create(@NotNull JavaType elementType) {
        return new JavaArrayType(elementType.getPsi().createArrayType());
    }

    @NotNull
    public JavaType getComponentType() {
        return JavaType.create(getPsi().getComponentType());
    }
}
