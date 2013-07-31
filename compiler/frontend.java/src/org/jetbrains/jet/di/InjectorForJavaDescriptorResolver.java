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

package org.jetbrains.jet.di;

import com.intellij.openapi.project.Project;
import org.jetbrains.jet.lang.resolve.BindingTrace;
import org.jetbrains.jet.lang.resolve.java.JavaClassFinderImpl;
import org.jetbrains.jet.lang.resolve.java.JavaDescriptorResolver;
import org.jetbrains.jet.lang.resolve.java.PsiClassFinderImpl;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaClassResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaAnnotationResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaCompileTimeConstResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaFunctionResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaValueParameterResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaTypeTransformer;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaSignatureResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.DeserializedDescriptorResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.AnnotationDescriptorDeserializer;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaNamespaceResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaSupertypeResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaConstructorResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaPropertyResolver;
import org.jetbrains.annotations.NotNull;
import javax.annotation.PreDestroy;

/* This file is generated by org.jetbrains.jet.generators.injectors.GenerateInjectors. DO NOT EDIT! */
public class InjectorForJavaDescriptorResolver {
    
    private final Project project;
    private final BindingTrace bindingTrace;
    private JavaClassFinderImpl javaClassFinder;
    private JavaDescriptorResolver javaDescriptorResolver;
    private PsiClassFinderImpl psiClassFinder;
    private JavaClassResolver javaClassResolver;
    private JavaAnnotationResolver javaAnnotationResolver;
    private JavaCompileTimeConstResolver javaCompileTimeConstResolver;
    private JavaFunctionResolver javaFunctionResolver;
    private JavaValueParameterResolver javaValueParameterResolver;
    private JavaTypeTransformer javaTypeTransformer;
    private JavaSignatureResolver javaSignatureResolver;
    private DeserializedDescriptorResolver deserializedDescriptorResolver;
    private AnnotationDescriptorDeserializer annotationDescriptorDeserializer;
    private JavaNamespaceResolver javaNamespaceResolver;
    private JavaSupertypeResolver javaSupertypeResolver;
    private JavaConstructorResolver javaConstructorResolver;
    private JavaPropertyResolver javaPropertyResolver;
    
    public InjectorForJavaDescriptorResolver(
        @NotNull Project project,
        @NotNull BindingTrace bindingTrace
    ) {
        this.project = project;
        this.bindingTrace = bindingTrace;
        this.javaClassFinder = new JavaClassFinderImpl();
        this.javaDescriptorResolver = new JavaDescriptorResolver();
        this.psiClassFinder = new PsiClassFinderImpl();
        this.javaClassResolver = new JavaClassResolver();
        this.javaAnnotationResolver = new JavaAnnotationResolver();
        this.javaCompileTimeConstResolver = new JavaCompileTimeConstResolver();
        this.javaFunctionResolver = new JavaFunctionResolver();
        this.javaValueParameterResolver = new JavaValueParameterResolver();
        this.javaTypeTransformer = new JavaTypeTransformer();
        this.javaSignatureResolver = new JavaSignatureResolver();
        this.deserializedDescriptorResolver = new DeserializedDescriptorResolver();
        this.annotationDescriptorDeserializer = new AnnotationDescriptorDeserializer();
        this.javaNamespaceResolver = new JavaNamespaceResolver();
        this.javaSupertypeResolver = new JavaSupertypeResolver();
        this.javaConstructorResolver = new JavaConstructorResolver();
        this.javaPropertyResolver = new JavaPropertyResolver();

        javaClassFinder.setPsiClassFinder(psiClassFinder);

        this.javaDescriptorResolver.setClassResolver(javaClassResolver);
        this.javaDescriptorResolver.setConstructorResolver(javaConstructorResolver);
        this.javaDescriptorResolver.setFunctionResolver(javaFunctionResolver);
        this.javaDescriptorResolver.setNamespaceResolver(javaNamespaceResolver);
        this.javaDescriptorResolver.setPropertiesResolver(javaPropertyResolver);

        this.psiClassFinder.setProject(project);

        javaClassResolver.setAnnotationResolver(javaAnnotationResolver);
        javaClassResolver.setFunctionResolver(javaFunctionResolver);
        javaClassResolver.setJavaClassFinder(javaClassFinder);
        javaClassResolver.setJavaDescriptorResolver(javaDescriptorResolver);
        javaClassResolver.setKotlinDescriptorResolver(deserializedDescriptorResolver);
        javaClassResolver.setNamespaceResolver(javaNamespaceResolver);
        javaClassResolver.setSignatureResolver(javaSignatureResolver);
        javaClassResolver.setSupertypesResolver(javaSupertypeResolver);
        javaClassResolver.setTrace(bindingTrace);

        javaAnnotationResolver.setClassResolver(javaClassResolver);
        javaAnnotationResolver.setCompileTimeConstResolver(javaCompileTimeConstResolver);

        javaCompileTimeConstResolver.setAnnotationResolver(javaAnnotationResolver);
        javaCompileTimeConstResolver.setClassResolver(javaClassResolver);

        javaFunctionResolver.setAnnotationResolver(javaAnnotationResolver);
        javaFunctionResolver.setParameterResolver(javaValueParameterResolver);
        javaFunctionResolver.setSignatureResolver(javaSignatureResolver);
        javaFunctionResolver.setTrace(bindingTrace);
        javaFunctionResolver.setTypeTransformer(javaTypeTransformer);

        javaValueParameterResolver.setTypeTransformer(javaTypeTransformer);

        javaTypeTransformer.setClassResolver(javaClassResolver);

        javaSignatureResolver.setTypeTransformer(javaTypeTransformer);

        deserializedDescriptorResolver.setAnnotationDeserializer(annotationDescriptorDeserializer);
        deserializedDescriptorResolver.setJavaClassResolver(javaClassResolver);
        deserializedDescriptorResolver.setJavaNamespaceResolver(javaNamespaceResolver);

        annotationDescriptorDeserializer.setJavaClassResolver(javaClassResolver);
        annotationDescriptorDeserializer.setPsiClassFinder(psiClassFinder);

        javaNamespaceResolver.setDeserializedDescriptorResolver(deserializedDescriptorResolver);
        javaNamespaceResolver.setJavaClassFinder(javaClassFinder);
        javaNamespaceResolver.setJavaDescriptorResolver(javaDescriptorResolver);
        javaNamespaceResolver.setTrace(bindingTrace);

        javaSupertypeResolver.setClassResolver(javaClassResolver);
        javaSupertypeResolver.setTrace(bindingTrace);
        javaSupertypeResolver.setTypeTransformer(javaTypeTransformer);

        javaConstructorResolver.setTrace(bindingTrace);
        javaConstructorResolver.setTypeTransformer(javaTypeTransformer);
        javaConstructorResolver.setValueParameterResolver(javaValueParameterResolver);

        javaPropertyResolver.setAnnotationResolver(javaAnnotationResolver);
        javaPropertyResolver.setTrace(bindingTrace);
        javaPropertyResolver.setTypeTransformer(javaTypeTransformer);

        psiClassFinder.initialize();

    }
    
    @PreDestroy
    public void destroy() {
    }
    
    public JavaDescriptorResolver getJavaDescriptorResolver() {
        return this.javaDescriptorResolver;
    }
    
    public PsiClassFinderImpl getPsiClassFinder() {
        return this.psiClassFinder;
    }
    
}
