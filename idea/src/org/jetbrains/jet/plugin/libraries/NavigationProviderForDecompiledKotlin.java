package org.jetbrains.jet.plugin.libraries;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.jet.lang.resolve.DescriptorUtils;
import org.jetbrains.jet.lang.resolve.java.PackageClassUtils;
import org.jetbrains.jet.lang.resolve.java.vfilefinder.VirtualFileFinder;
import org.jetbrains.jet.lang.resolve.name.FqName;

import static org.jetbrains.jet.lang.resolve.DescriptorUtils.getFQName;

//TODO: better class name
public final class NavigationProviderForDecompiledKotlin {

    @Nullable
    public static JetDeclaration provideDeclarationForReference(
            @NotNull JetReferenceExpression expression,
            @NotNull DeclarationDescriptor referencedDescriptor
    ) {
        JetDeclaration declarationFromDecompiledClassFile = getDeclarationFromDecompiledClassFile(expression, referencedDescriptor);
        if (declarationFromDecompiledClassFile == null) {
            return null;
        }
        JetDeclaration sourceElement = declarationFromDecompiledClassFile.accept(new SourceForDecompiledExtractingVisitor(), null);
        return sourceElement != null ? sourceElement : declarationFromDecompiledClassFile;
    }

    @Nullable
    private static JetDeclaration getDeclarationFromDecompiledClassFile(
            @NotNull JetReferenceExpression expression,
            @NotNull DeclarationDescriptor referencedDescriptor
    ) {
        DeclarationDescriptor effectiveReferencedDescriptor = getEffectiveReferencedDescriptor(referencedDescriptor);
        VirtualFile virtualFile = findVirtualForDescriptor(expression, effectiveReferencedDescriptor);
        if (virtualFile == null) return null;
        JetDecompiledData data = JetDecompiledData.getDecompiledData(virtualFile, expression.getProject());
        PsiFile psiFile = expression.getManager().findFile(virtualFile);
        if (psiFile instanceof JetFile) {
            JetDeclaration jetDeclaration = data.getDeclarationForDescriptor((JetFile) psiFile, effectiveReferencedDescriptor);
            if (jetDeclaration != null) {
                return jetDeclaration;
            }
            else {
                //TODO: log error
            }
        }
        else {
            //TODO: LOG ERROR
            return null;
        }
        return null;
    }

    //clearer logic
    @NotNull
    private static DeclarationDescriptor getEffectiveReferencedDescriptor(@NotNull DeclarationDescriptor referencedDescriptor) {
        if (referencedDescriptor instanceof VariableDescriptorForObject) {
            return ((VariableDescriptorForObject) referencedDescriptor).getObjectClass();
        }
        return referencedDescriptor;
    }

    @Nullable
    private static VirtualFile findVirtualForDescriptor(
            @NotNull JetReferenceExpression expression,
            @NotNull DeclarationDescriptor referencedDescriptor
    ) {
        FqName containerFqName = getContainerFqName(referencedDescriptor);
        if (containerFqName == null) {
            return null;
        }
        VirtualFileFinder fileFinder = ServiceManager.getService(expression.getProject(), VirtualFileFinder.class);
        VirtualFile virtualFile = fileFinder.find(containerFqName);
        if (virtualFile == null) {
            return null;
        }
        return virtualFile;
    }

    @Nullable
    private static FqName getContainerFqName(@NotNull DeclarationDescriptor referencedDescriptor) {
        ClassOrNamespaceDescriptor
                containerDescriptor = DescriptorUtils.getParentOfType(referencedDescriptor, ClassOrNamespaceDescriptor.class, false);
        if (containerDescriptor instanceof NamespaceDescriptor) {
            return PackageClassUtils.getPackageClassFqName(getFQName(containerDescriptor).toSafe());
        }
        if (containerDescriptor instanceof ClassDescriptor) {
            //TODO: test this code
            ClassKind classKind = ((ClassDescriptor) containerDescriptor).getKind();
            //TODO: be more precise
            if (classKind == ClassKind.CLASS_OBJECT || classKind == ClassKind.ENUM_ENTRY) {
                return getContainerFqName(containerDescriptor.getContainingDeclaration());
            }
            return getFQName(containerDescriptor).toSafe();
        }
        return null;
    }

    private static class SourceForDecompiledExtractingVisitor extends JetVisitor<JetDeclaration, Void> {
        @Override
        public JetDeclaration visitNamedFunction(JetNamedFunction function, Void data) {
            return JetSourceNavigationHelper.getSourceFunction(function);
        }

        @Override
        public JetDeclaration visitProperty(JetProperty property, Void data) {
            return JetSourceNavigationHelper.getSourceProperty(property);
        }

        @Override
        public JetDeclaration visitObjectDeclaration(JetObjectDeclaration declaration, Void data) {
            return JetSourceNavigationHelper.getSourceClassOrObject(declaration);
        }

        @Override
        public JetDeclaration visitClass(JetClass klass, Void data) {
            return JetSourceNavigationHelper.getSourceClassOrObject(klass);
        }
    }

    private NavigationProviderForDecompiledKotlin() {
    }
}
