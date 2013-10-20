package sparta.checkers;

import checkers.basetype.BaseTypeVisitor;
import checkers.quals.StubFiles;
import checkers.quals.TypeQualifiers;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedTypeVariable;
import checkers.types.AnnotatedTypeMirror.AnnotatedWildcardType;

import javacutils.AnnotationUtils;
import javacutils.TreeUtils;

import java.util.Set;

import javax.lang.model.type.TypeKind;

import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.Tree;

@TypeQualifiers({ Source.class, Sink.class, PolySource.class, PolySink.class })
@StubFiles("flow.astub")
public class FlowShow extends FlowChecker {

    @Override
    protected BaseTypeVisitor<?> createSourceVisitor() {
        return new FlowShowVisitor(this);
    }

    protected class FlowShowVisitor extends BaseTypeVisitor<FlowAnnotatedTypeFactory> {

        public FlowShowVisitor(FlowShow checker) {
            super(checker);
        }

        @Override
        public FlowAnnotatedTypeFactory createTypeFactory() {
            return new FlowAnnotatedTypeFactory(FlowShow.this);
        }

        @Override
        public Void scan(Tree tree, Void p) {
            super.scan(tree, p);
            if (TreeUtils.isExpressionTree(tree) && !(tree instanceof AnnotationTree)
                    && !(tree.getKind() == Tree.Kind.NULL_LITERAL)) {
                AnnotatedTypeMirror type = this.atypeFactory.getAnnotatedType(tree);
                if (type.getKind() == TypeKind.WILDCARD) {
                    type = ((AnnotatedWildcardType) type).getEffectiveExtendsBound();
                } else if (type.getKind() == TypeKind.TYPEVAR) {
                    type = ((AnnotatedTypeVariable) type).getEffectiveUpperBound();
                }

                boolean show = false;

                if (!AnnotationUtils.areSame(type.getAnnotationInHierarchy(atypeFactory.NOSOURCE), atypeFactory.NOSOURCE)) {
                    show = true;
                }
                if (!AnnotationUtils.areSame(type.getAnnotationInHierarchy(atypeFactory.NOSINK), atypeFactory.NOSINK)) {
                    show = true;
                }
                if (show) {
                    Set<FlowPermission> src = Flow.getSources(type);
                    String stsrc = src.isEmpty() ? "NONE" : src.toString();
                    Set<FlowPermission> snk = Flow.getSinks(type);
                    String stsnk = snk.isEmpty() ? "NONE" : snk.toString();
                    String msg = "FLOW TREE " + tree + " KIND " + tree.getKind() + " SOURCES "
                            + stsrc + " SINKS " + stsnk;
                    trees.printMessage(javax.tools.Diagnostic.Kind.OTHER, msg, tree, currentRoot);
                }
            }
            return null;
        }
    }
}