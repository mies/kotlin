package org.jetbrains.jet.plugin.stubs.builder;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.compiled.ClsStubBuilderFactory;
import com.intellij.psi.stubs.PsiFileStub;
import com.intellij.util.cls.ClsFormatException;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.resolve.java.resolver.KotlinClassFileHeader;
import org.jetbrains.jet.lang.resolve.name.FqName;

public class JetClsStubBuilderFactory extends ClsStubBuilderFactory<JetFile> {
    @Nullable
    @Override
    public PsiFileStub<JetFile> buildFileStub(VirtualFile file, byte[] bytes) throws ClsFormatException {
        KotlinClassFileHeader header = KotlinClassFileHeader.readKotlinHeaderFromClassFile(file);
        if (!header.isKotlinCompiledFile()) {
            return null;
        }
        FqName classFqName = header.getJvmClassName().getFqName();
        FqName packageFqName = classFqName.parent();
        if (header.getType() == KotlinClassFileHeader.HeaderType.PACKAGE) {
            return new CompiledPackageClassStubBuilder(header.readPackageData(), packageFqName).createStub();
        }
        if (header.getType() == KotlinClassFileHeader.HeaderType.CLASS) {
            return new CompiledClassStubBuilder(header.readClassData(), classFqName, packageFqName).createStub();
        }
        throw new IllegalStateException("Should have processed " + file.getPath());
    }

    @Override
    public boolean canBeProcessed(VirtualFile file, byte[] bytes) {
        return true;
    }

    @Override
    public boolean isInnerClass(VirtualFile file) {
        //NOTE: copy of DefaultClsStubBuilderFactory#isInnerClass
        //NOTE: it seems we are not going to process any inner classes
        String name = file.getNameWithoutExtension();
        int len = name.length();
        int idx = name.indexOf('$');

        while (idx > 0) {
            if (idx + 1 < len && Character.isDigit(name.charAt(idx + 1))) return true;
            idx = name.indexOf('$', idx + 1);
        }
        return false;
    }
}
