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

package org.jetbrains.jet.plugin.stubs.builder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.descriptors.serialization.NameResolver;
import org.jetbrains.jet.descriptors.serialization.PackageData;
import org.jetbrains.jet.descriptors.serialization.ProtoBuf;
import org.jetbrains.jet.lang.psi.stubs.PsiJetFileStub;
import org.jetbrains.jet.lang.psi.stubs.impl.PsiJetFileStubImpl;
import org.jetbrains.jet.lang.resolve.java.resolver.KotlinClassFileHeader;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.name.Name;

public final class CompiledPackageClassStubBuilder extends CompiledStubBuilderBase {

    @NotNull
    private final ProtoBuf.Package packageProto;
    @NotNull
    private final NameResolver nameResolver;

    public CompiledPackageClassStubBuilder(@NotNull KotlinClassFileHeader header) {
        super(header);
        PackageData packageData = header.readPackageData();
        assert packageData != null;
        this.packageProto = packageData.getPackageProto();
        this.nameResolver = packageData.getNameResolver();
    }

    public PsiJetFileStub createStub() {
        PsiJetFileStubImpl fileStub = createFileStub();
        for (ProtoBuf.Callable callableProto : packageProto.getMemberList()) {
            createCallableStub(fileStub, callableProto);
        }
        return fileStub;
    }

    @NotNull
    @Override
    protected NameResolver getNameResolver() {
        return nameResolver;
    }

    @Override
    @Nullable
    protected FqName getInternalFqName(@NotNull String callableName) {
        return packageFqName.child(Name.identifier(callableName));
    }
}
