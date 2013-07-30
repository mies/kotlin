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

package org.jetbrains.jet.plugin.completion;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.JavaCompletionContributor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Conditions;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.lazy.ResolveSession;
import org.jetbrains.jet.lang.resolve.lazy.ResolveSessionUtils;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.scopes.JetScope;
import org.jetbrains.jet.lang.types.ErrorUtils;
import org.jetbrains.jet.lang.types.JetType;
import org.jetbrains.jet.lang.types.expressions.ExpressionTypingUtils;
import org.jetbrains.jet.lexer.JetTokens;
import org.jetbrains.jet.plugin.codeInsight.TipsManager;
import org.jetbrains.jet.plugin.completion.weigher.JetCompletionSorting;
import org.jetbrains.jet.plugin.project.WholeProjectAnalyzerFacade;
import org.jetbrains.jet.plugin.references.JetSimpleNameReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static org.jetbrains.jet.lang.resolve.lazy.ResolveSessionUtils.getClassOrObjectDescriptorsByFqName;

import static org.jetbrains.jet.plugin.caches.TopLevelDeclarationIndexUtils.*;

public class CompletionSession {
    @Nullable
    private final DeclarationDescriptor inDescriptor;
    private final CompletionParameters parameters;
    private final JetCompletionResultSet jetResult;
    private final JetSimpleNameReference jetReference;

    public CompletionSession(
            @NotNull CompletionParameters parameters,
            @NotNull CompletionResultSet result,
            @NotNull JetSimpleNameReference jetReference,
            @NotNull PsiElement position
    ) {
        this.parameters = parameters;
        this.jetReference = jetReference;

        ResolveSession resolveSession = WholeProjectAnalyzerFacade.getLazyResolveSessionForFile((JetFile) position.getContainingFile());
        BindingContext expressionBindingContext = ResolveSessionUtils.resolveToExpression(resolveSession, jetReference.getExpression());
        JetScope scope = expressionBindingContext.get(BindingContext.RESOLUTION_SCOPE, jetReference.getExpression());

        inDescriptor = scope != null ? scope.getContainingDeclaration() : null;

        this.jetResult = new JetCompletionResultSet(
                JetCompletionSorting.addJetSorting(parameters, result),
                resolveSession,
                expressionBindingContext, new Condition<DeclarationDescriptor>() {
            @Override
            public boolean value(DeclarationDescriptor descriptor) {
                return isVisibleDescriptor(descriptor);
            }
        });
    }

    void completeForReference() {
        if (isOnlyKeywordCompletion(getPosition())) {
            return;
        }

        if (shouldRunOnlyTypeCompletion()) {
            if (parameters.getInvocationCount() >= 2) {
                JetTypesCompletionHelper.addJetTypes(parameters, jetResult);
            }
            else {
                addReferenceVariants(new Condition<DeclarationDescriptor>() {
                    @Override
                    public boolean value(DeclarationDescriptor descriptor) {
                        return isPartOfTypeDeclaration(descriptor);
                    }
                });
                JavaCompletionContributor.advertiseSecondCompletion(parameters.getPosition().getProject(), jetResult.getResult());
            }

            return;
        }

        addReferenceVariants(Conditions.<DeclarationDescriptor>alwaysTrue());

        String prefix = jetResult.getResult().getPrefixMatcher().getPrefix();

        // Try to avoid computing not-imported descriptors for empty prefix
        if (prefix.isEmpty()) {
            if (parameters.getInvocationCount() < 2) {
                return;
            }

            if (PsiTreeUtil.getParentOfType(jetReference.getExpression(), JetDotQualifiedExpression.class) == null) {
                return;
            }
        }

        if (shouldRunTopLevelCompletion()) {
            JetTypesCompletionHelper.addJetTypes(parameters, jetResult);
            addTopLevelFunctions();
            addTopLevelObjects();
        }

        if (shouldRunExtensionsCompletion()) {
            addExtensionFunctions();
        }
    }

    private static boolean isOnlyKeywordCompletion(PsiElement position) {
        return PsiTreeUtil.getParentOfType(position, JetModifierList.class) != null;
    }

    private void addExtensionFunctions() {
        JetSimpleNameExpression expression = jetReference.getExpression();
        ResolveSession resolveSession = getResolveSession();
        BindingContext context = ResolveSessionUtils.resolveToExpression(resolveSession, expression);
        JetExpression receiverExpression = expression.getReceiverExpression();
        if (receiverExpression == null) {
            return;
        }
        JetType expressionType = context.get(BindingContext.EXPRESSION_TYPE, receiverExpression);
        JetScope scope = context.get(BindingContext.RESOLUTION_SCOPE, receiverExpression);

        if (expressionType == null || scope == null || ErrorUtils.isErrorType(expressionType)) {
            return;
        }

        Project project = getPosition().getProject();
        Collection<FqName> functionFqNames =
                getExtensionFunctionFqNames(jetResult.getShortNameFilter(), GlobalSearchScope.allScope(project));
        Collection<DeclarationDescriptor> resultDescriptors = new ArrayList<DeclarationDescriptor>();
        for (FqName functionFQN : functionFqNames) {
            for (CallableDescriptor functionDescriptor : ExpressionTypingUtils.canFindSuitableCall(
                    functionFQN, project, receiverExpression, expressionType, scope,
                    resolveSession.getRootModuleDescriptor())) {
                resultDescriptors.add(functionDescriptor);
            }
        }
        jetResult.addAllElements(resultDescriptors);
    }

    public static boolean isPartOfTypeDeclaration(@NotNull DeclarationDescriptor descriptor) {
        if (descriptor instanceof NamespaceDescriptor || descriptor instanceof TypeParameterDescriptor) {
            return true;
        }

        if (descriptor instanceof ClassDescriptor) {
            ClassDescriptor classDescriptor = (ClassDescriptor) descriptor;
            ClassKind kind = classDescriptor.getKind();
            return !(kind == ClassKind.OBJECT || kind == ClassKind.CLASS_OBJECT);
        }

        return false;
    }

    private void addTopLevelFunctions() {
        Project project = getPosition().getProject();
        Set<FunctionDescriptor> result = Sets.newHashSet();
        Collection<FqName> functionFqNames =
                getTopLevelNonExtensionFunctionFqNames(jetResult.getShortNameFilter(), GlobalSearchScope.allScope(project));
        for (FqName topLevelFunctionFqName : functionFqNames) {
            FqName packageFqName = topLevelFunctionFqName.parent();
            NamespaceDescriptor packageDescriptor = getResolveSession().getPackageDescriptorByFqName(packageFqName);
            assert packageDescriptor != null : "There's a function in stub index with invalid package: " + topLevelFunctionFqName;
            JetScope memberScope = packageDescriptor.getMemberScope();
            result.addAll(memberScope.getFunctions(topLevelFunctionFqName.shortName()));
        }
        jetResult.addAllElements(result);
    }

    private void addTopLevelObjects() {
        Project project = getPosition().getProject();
        ResolveSession resolveSession = getResolveSession();
        Condition<String> acceptedShortNameCondition = jetResult.getShortNameFilter();
        Collection<FqName> topLevelObjectFqNames = getTopLevelObjectFqNames(acceptedShortNameCondition, GlobalSearchScope.allScope(project));
        for (FqName objectDeclaration : topLevelObjectFqNames) {
            jetResult.addAllElements(getClassOrObjectDescriptorsByFqName(resolveSession, objectDeclaration, true));
        }
    }

    private boolean shouldRunOnlyTypeCompletion() {
        // Check that completion in the type annotation context and if there's a qualified
        // expression we are at first of it
        JetTypeReference typeReference = PsiTreeUtil.getParentOfType(getPosition(), JetTypeReference.class);
        if (typeReference != null) {
            JetSimpleNameExpression firstPartReference = PsiTreeUtil.findChildOfType(typeReference, JetSimpleNameExpression.class);
            return firstPartReference == jetReference.getExpression();
        }

        return false;
    }

    private boolean shouldRunTopLevelCompletion() {
        if (parameters.getInvocationCount() < 2) {
            return false;
        }

        PsiElement element = getPosition();
        if (getPosition().getNode().getElementType() == JetTokens.IDENTIFIER) {
            if (element.getParent() instanceof JetSimpleNameExpression) {
                JetSimpleNameExpression nameExpression = (JetSimpleNameExpression) element.getParent();

                // Top level completion should be executed for simple name which is not in qualified expression
                if (PsiTreeUtil.getParentOfType(nameExpression, JetQualifiedExpression.class) != null) {
                    return false;
                }

                // Don't call top level completion in qualified named position of user type
                PsiElement parent = nameExpression.getParent();
                if (parent instanceof JetUserType && ((JetUserType) parent).getQualifier() != null) {
                    return false;
                }

                return true;
            }
        }

        return false;
    }

    private boolean shouldRunExtensionsCompletion() {
        return !(parameters.getInvocationCount() <= 1 && jetResult.getResult().getPrefixMatcher().getPrefix().length() < 3);
    }

    private void addReferenceVariants(@NotNull final Condition<DeclarationDescriptor> filterCondition) {
        Collection<DeclarationDescriptor> descriptors = TipsManager.getReferenceVariants(
                jetReference.getExpression(), getExpressionBindingContext());

        Collection<DeclarationDescriptor> filterDescriptors = Collections2.filter(descriptors, new Predicate<DeclarationDescriptor>() {
            @Override
            public boolean apply(@Nullable DeclarationDescriptor descriptor) {
                return descriptor != null && filterCondition.value(descriptor);
            }
        });

        jetResult.addAllElements(filterDescriptors);
    }

    private boolean isVisibleDescriptor(DeclarationDescriptor descriptor) {
        if (parameters.getInvocationCount() >= 2) {
            // Show everything if user insist on showing completion list
            return true;
        }

        if (descriptor instanceof DeclarationDescriptorWithVisibility) {
            if (inDescriptor != null) {
                //noinspection ConstantConditions
                return Visibilities.isVisible((DeclarationDescriptorWithVisibility) descriptor, inDescriptor);
            }
        }

        return true;
    }

    private BindingContext getExpressionBindingContext() {
        return jetResult.getBindingContext();
    }

    private ResolveSession getResolveSession() {
        return jetResult.getResolveSession();
    }

    private PsiElement getPosition() {
        return parameters.getPosition();
    }

    public JetCompletionResultSet getJetResult() {
        return jetResult;
    }

    public CompletionParameters getParameters() {
        return parameters;
    }
}
