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

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMember;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.descriptors.Visibility;
import org.jetbrains.jet.lang.resolve.name.Name;

import java.util.Collection;

public abstract class JavaMemberImpl extends JavaElementImpl implements JavaMember, JavaModifierListOwner {
    protected JavaMemberImpl(@NotNull PsiMember psiMember) {
        super(psiMember);
    }

    @NotNull
    @Override
    public PsiMember getPsi() {
        return (PsiMember) super.getPsi();
    }

    @NotNull
    @Override
    public Name getName() {
        String name = getPsi().getName();
        assert name != null : "Member must have a name: " + getPsi();
        return Name.identifier(name);
    }

    // TODO: NotNull?
    @Nullable
    @Override
    public JavaClass getContainingClass() {
        PsiClass psiClass = getPsi().getContainingClass();
        return psiClass == null ? null : new JavaClass(psiClass);
    }

    @Override
    public boolean isAbstract() {
        return JavaElementUtil.isAbstract(this);
    }

    @Override
    public boolean isStatic() {
        return JavaElementUtil.isStatic(this);
    }

    @Override
    public boolean isFinal() {
        return JavaElementUtil.isFinal(this);
    }

    @NotNull
    @Override
    public Visibility getVisibility() {
        return JavaElementUtil.getVisibility(this);
    }

    @NotNull
    @Override
    public Collection<JavaAnnotation> getAnnotations() {
        return JavaElementUtil.getAnnotations(this);
    }
}
