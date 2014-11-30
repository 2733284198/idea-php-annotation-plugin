package de.espend.idea.php.annotation.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamespace;
import com.jetbrains.php.lang.psi.stubs.indexes.PhpNamespaceIndex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class PhpIndexUtil {

    /**
     * Collect PhpClass which are inside current namespace and in sub-namespaces
     *
     * @param project current project
     * @param namespaceName namespace name should start with \ and not end with "\"
     * @return classes inside namespace and sub-namespace
     */
    public static Collection<PhpClass> getPhpClassInsideNamespace(Project project, String namespaceName) {
        return getPhpClassInsideNamespace(project, PhpIndex.getInstance(project), namespaceName, 10);
    }

    private static Collection<PhpClass> getPhpClassInsideNamespace(Project project, PhpIndex phpIndex, String namespaceName, int maxDeep) {

        final Collection<PhpClass> phpClasses = new ArrayList<PhpClass>();

        if(maxDeep-- <= 0) {
            return phpClasses;
        }

        StubIndex.getInstance().process(PhpNamespaceIndex.KEY, namespaceName.toLowerCase(), project, phpIndex.getSearchScope(), new Processor<PhpNamespace>() {
            @Override
            public boolean process(PhpNamespace phpNamespace) {
                phpClasses.addAll(PsiTreeUtil.getChildrenOfTypeAsList(phpNamespace.getStatements(), PhpClass.class));
                return true;
            }
        });

        for(String ns: phpIndex.getChildNamespacesByParentName(namespaceName + "\\")) {
            phpClasses.addAll(getPhpClassInsideNamespace(project, phpIndex, namespaceName + "\\" + ns, maxDeep));
        }

        return phpClasses;
    }

}

