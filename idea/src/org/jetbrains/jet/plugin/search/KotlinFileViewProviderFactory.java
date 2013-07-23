package org.jetbrains.jet.plugin.search;

import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.plugin.JetLanguage;
import org.jetbrains.jet.plugin.libraries.JetContentBasedFileSubstitutor;

public class KotlinFileViewProviderFactory implements FileViewProviderFactory {
    @Override
    public FileViewProvider createFileViewProvider(
            @NotNull VirtualFile file, Language language, @NotNull PsiManager manager, boolean physical
    ) {
        if (language != JetLanguage.INSTANCE) {
            return null;
        }
        if (JetContentBasedFileSubstitutor.isKotlinCompiledFile(file)) {
            return new SingleRootFileViewProvider(manager, file, physical) {
                @NotNull
                @Override
                public Language getBaseLanguage() {
                    return JetLanguage.INSTANCE;
                }

                @Nullable
                @Override
                protected PsiFile createFile(@NotNull Language lang) {
                    return new JetFile(this, true);
                }

                @Nullable
                @Override
                protected PsiFile createFile(
                        @NotNull Project project, @NotNull VirtualFile file, @NotNull FileType fileType
                ) {
                    return new JetFile(this, true);
                }
            };
        }
        return new SingleRootFileViewProvider(manager, file, physical);
    }
}
