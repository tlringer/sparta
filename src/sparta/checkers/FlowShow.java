package sparta.checkers;

import java.util.List;

import javax.lang.model.type.TypeKind;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;

import checkers.quals.StubFiles;
import checkers.quals.TypeQualifiers;
import checkers.source.SourceVisitor;
import checkers.types.AnnotatedTypeFactory;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedTypeVariable;
import checkers.types.AnnotatedTypeMirror.AnnotatedWildcardType;
import checkers.util.AnnotationUtils;
import checkers.util.TreeUtils;

import sparta.checkers.quals.Sinks;
import sparta.checkers.quals.Sinks.SPARTA_Permission;
import sparta.checkers.quals.Sources;
import sparta.checkers.quals.Sources.SPARTA_Permission;
import sparta.checkers.quals.PolySinks;
import sparta.checkers.quals.PolySources;

@TypeQualifiers({Sources.class, Sinks.class,
    PolySources.class, PolySinks.class})
@StubFiles("flow.astub")
public class FlowShow extends FlowChecker {
    @Override
    public AnnotatedTypeFactory createFactory(CompilationUnitTree root) {
        return new FlowAnnotatedTypeFactory(this, root);
    }

    @Override
    protected SourceVisitor<?, ?> createSourceVisitor(CompilationUnitTree root) {
        return new FlowShowVisitor(this, root);
    }

    protected class FlowShowVisitor extends SourceVisitor<Void, Void> {

        private final Trees treemsg;

        public FlowShowVisitor(FlowShow checker, CompilationUnitTree root) {
            super(checker, root);
            treemsg = Trees.instance(processingEnv);
        }

        @Override
        public Void scan(Tree tree, Void p) {
            super.scan(tree, p);
            if (TreeUtils.isExpressionTree(tree) &&
                    !(tree instanceof AnnotationTree) &&
                    !(tree.getKind()==Tree.Kind.NULL_LITERAL)) {
                AnnotatedTypeMirror type = this.atypeFactory.getAnnotatedType(tree);
                if (type.getKind() == TypeKind.WILDCARD) {
                    type = ((AnnotatedWildcardType)type).getEffectiveExtendsBound();
                } else if (type.getKind() == TypeKind.TYPEVAR) {
                    type = ((AnnotatedTypeVariable)type).getEffectiveUpperBound();
                }

                boolean show = false;

                if (!AnnotationUtils.areSame(type.getAnnotationInHierarchy(NOFLOWSOURCES), NOFLOWSOURCES)) {
                    show = true;
                }
                if (!AnnotationUtils.areSame(type.getAnnotationInHierarchy(NOFLOWSINKS), NOFLOWSINKS)) {
                    show = true;
                }
                if (show) {
                    List<SPARTA_Permission> src = getSources(type);
                    String stsrc = src.isEmpty() ? "NONE" : src.toString();
                    List<SPARTA_Permission> snk = getSinks(type);
                    String stsnk = snk.isEmpty() ? "NONE" : snk.toString();
                    String msg = "FLOW TREE " + tree +
                            " KIND " + tree.getKind() +
                            " SOURCES " + stsrc +
                            " SINKS " + stsnk;
                    treemsg.printMessage(javax.tools.Diagnostic.Kind.OTHER, msg, tree, currentRoot);
                }
            }
            return null;
        }
    }
}