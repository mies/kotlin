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

import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Processor;
import com.intellij.util.containers.HashSet;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.asJava.JavaElementFinder;
import org.jetbrains.jet.lang.descriptors.ClassDescriptor;
import org.jetbrains.jet.lang.psi.JetClassOrObject;
import org.jetbrains.jet.lang.psi.JetPsiUtil;
import org.jetbrains.jet.lang.resolve.lazy.KotlinCodeAnalyzer;
import org.jetbrains.jet.lang.resolve.lazy.ResolveSessionUtils;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.plugin.caches.resolve.IDELightClassGenerationSupport;
import org.jetbrains.jet.plugin.stubindex.JetClassShortNameIndex;
import org.jetbrains.jet.plugin.stubindex.JetFullClassNameIndex;
import org.jetbrains.jet.plugin.stubindex.JetTopLevelNonExtensionFunctionShortNameIndex;

import java.util.*;

/**
 * Will provide both java elements from jet context and some special declarations special to jet.
 * All those declaration are planned to be used in completion.
 */
public class JetShortNamesCache extends PsiShortNamesCache {

    public static JetShortNamesCache getKotlinInstance(@NotNull Project project) {
        PsiShortNamesCache[] extensions = Extensions.getArea(project).getExtensionPoint(PsiShortNamesCache.EP_NAME).getExtensions();
        for (PsiShortNamesCache extension : extensions) {
            if (extension instanceof JetShortNamesCache) {
                return (JetShortNamesCache) extension;
            }
        }
        throw new IllegalStateException(JetShortNamesCache.class.getSimpleName() + " is not found for project " + project);
    }

    private static final PsiMethod[] NO_METHODS = new PsiMethod[0];
    private static final PsiField[] NO_FIELDS = new PsiField[0];
    private final Project project;

    public JetShortNamesCache(Project project) {
        this.project = project;
    }

    /**
     * Return jet class names form jet project sources which should be visible from java.
     */
    @NotNull
    @Override
    public String[] getAllClassNames() {
        Collection<String> classNames = JetClassShortNameIndex.getInstance().getAllKeys(project);

        // .namespace classes can not be indexed, since they have no explicit declarations
        IDELightClassGenerationSupport lightClassGenerationSupport = IDELightClassGenerationSupport.getInstanceForIDE(project);
        Set<String> packageClassShortNames =
                lightClassGenerationSupport.getAllPossiblePackageClasses(GlobalSearchScope.allScope(project)).keySet();
        classNames.addAll(packageClassShortNames);

        return ArrayUtil.toStringArray(classNames);
    }

    /**
     * Return class names form jet sources in given scope which should be visible as Java classes.
     */
    @NotNull
    @Override
    public PsiClass[] getClassesByName(@NotNull @NonNls String name, @NotNull GlobalSearchScope scope) {
        List<PsiClass> result = new ArrayList<PsiClass>();

        IDELightClassGenerationSupport lightClassGenerationSupport = IDELightClassGenerationSupport.getInstanceForIDE(project);
        MultiMap<String, FqName> packageClasses = lightClassGenerationSupport.getAllPossiblePackageClasses(scope);

        // .namespace classes can not be indexed, since they have no explicit declarations
        Collection<FqName> fqNames = packageClasses.get(name);
        if (!fqNames.isEmpty()) {
            for (FqName fqName : fqNames) {
                PsiClass psiClass = JavaElementFinder.getInstance(project).findClass(fqName.asString(), scope);
                if (psiClass != null) {
                    result.add(psiClass);
                }
            }
        }

        // Quick check for classes from getAllClassNames()
        Collection<JetClassOrObject> classOrObjects = JetClassShortNameIndex.getInstance().get(name, project, scope);
        if (classOrObjects.isEmpty()) {
            return result.toArray(new PsiClass[result.size()]);
        }

        for (JetClassOrObject classOrObject : classOrObjects) {
            FqName fqName = classOrObject.getFqName();
            if (fqName != null) {
                assert fqName.shortName().asString().equals(name) : "A declaration obtained from index has non-matching name:\n" +
                                                                    "in index: " + name + "\n" +
                                                                    "declared: " + fqName.shortName() + "(" + fqName + ")";
                PsiClass psiClass = JavaElementFinder.getInstance(project).findClass(fqName.asString(), scope);
                if (psiClass != null) {
                    result.add(psiClass);
                }
            }
        }

        return result.toArray(new PsiClass[result.size()]);
    }

    @Override
    public void getAllClassNames(@NotNull HashSet<String> destination) {
        destination.addAll(Arrays.asList(getAllClassNames()));
    }

    public Collection<ClassDescriptor> getJetClassesDescriptors(
            @NotNull Condition<String> acceptedShortNameCondition,
            @NotNull KotlinCodeAnalyzer analyzer,
            @NotNull GlobalSearchScope searchScope
    ) {
        Collection<ClassDescriptor> classDescriptors = new ArrayList<ClassDescriptor>();

        for (String fqName : JetFullClassNameIndex.getInstance().getAllKeys(project)) {
            FqName classFQName = new FqName(fqName);
            if (acceptedShortNameCondition.value(classFQName.shortName().asString())) {
                classDescriptors.addAll(getJetClassesDescriptorsByFQName(analyzer, classFQName, searchScope));
            }
        }

        return classDescriptors;
    }

    private Collection<ClassDescriptor> getJetClassesDescriptorsByFQName(
            @NotNull KotlinCodeAnalyzer analyzer, @NotNull FqName classFQName, @NotNull GlobalSearchScope searchScope) {
        Collection<JetClassOrObject> jetClassOrObjects = JetFullClassNameIndex.getInstance().get(
                classFQName.asString(), project, searchScope);

        if (jetClassOrObjects.isEmpty()) {
            // This fqn is absent in caches, dead or not in scope
            return Collections.emptyList();
        }

        // Note: Can't search with psi element as analyzer could be built over temp files
        return ResolveSessionUtils.getClassDescriptorsByFqName(analyzer, classFQName);
    }

    @NotNull
    @Override
    public PsiMethod[] getMethodsByName(@NonNls @NotNull String name, @NotNull GlobalSearchScope scope) {
        return NO_METHODS;
    }

    @NotNull
    @Override
    public PsiMethod[] getMethodsByNameIfNotMoreThan(@NonNls @NotNull String name, @NotNull GlobalSearchScope scope, int maxCount) {
        return NO_METHODS;
    }

    @NotNull
    @Override
    public PsiField[] getFieldsByNameIfNotMoreThan(@NonNls @NotNull String s, @NotNull GlobalSearchScope scope, int i) {
        return NO_FIELDS;
    }

    @Override
    public boolean processMethodsWithName(
            @NonNls @NotNull String name,
            @NotNull GlobalSearchScope scope,
            @NotNull Processor<PsiMethod> processor
    ) {
        return false;
    }

    @NotNull
    @Override
    public String[] getAllMethodNames() {
        return ArrayUtil.EMPTY_STRING_ARRAY;
    }

    @Override
    public void getAllMethodNames(@NotNull HashSet<String> set) {
        set.addAll(JetTopLevelNonExtensionFunctionShortNameIndex.getInstance().getAllKeys(project));
    }

    @NotNull
    @Override
    public PsiField[] getFieldsByName(@NotNull @NonNls String name, @NotNull GlobalSearchScope scope) {
        return NO_FIELDS;
    }

    @NotNull
    @Override
    public String[] getAllFieldNames() {
        return ArrayUtil.EMPTY_STRING_ARRAY;
    }

    @Override
    public void getAllFieldNames(@NotNull HashSet<String> set) {
    }
}
