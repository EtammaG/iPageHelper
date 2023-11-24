package com.etammag.ipagehelper;

import com.google.auto.service.AutoService;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Set;

@SupportedAnnotationTypes("com.etammag.ipagehelper.Page")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class PageProcessor extends AbstractProcessor {

    private Trees trees;
    private TreeMaker treeMaker;
    private Names names;
    private Types typeUtils;
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.trees = Trees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
        this.typeUtils = processingEnv.getTypeUtils();
        this.elementUtils = processingEnv.getElementUtils();
    }

    @Override
    public synchronized boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement typeElement : annotations) {
            roundEnv.getElementsAnnotatedWith(typeElement).forEach(element -> {
                if (element.getKind().isInterface()) {
                    ((JCTree) trees.getTree(element)).accept(new InterfaceTranslator());
                }
            });
        }
        return false;
    }

    private class InterfaceTranslator extends TreeTranslator {

        @Override
        public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
            super.visitClassDef(jcClassDecl);
            for (JCTree jcTree : jcClassDecl.defs) {
                if (jcTree instanceof JCTree.JCMethodDecl) {
                    JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) jcTree;
                    if (!typeUtils.isSubtype(
                            typeUtils.erasure(jcMethodDecl.getReturnType().type),
                            typeUtils.erasure(elementUtils.getTypeElement("java.util.List").asType())
                    )) continue;
                    jcClassDecl.defs = jcClassDecl.defs.append(generatePage(jcMethodDecl));
                    jcClassDecl.defs = jcClassDecl.defs.append(generateCount(jcMethodDecl));
                }
            }
        }

        private JCTree.JCMethodDecl generatePage(JCTree.JCMethodDecl jcMethodDecl) {
            JCTree.JCModifiers modifiers = treeMaker.Modifiers(Flags.DEFAULT);
            JCTree.JCExpression returnType = jcMethodDecl.restype;
            Name name = names.fromString(jcMethodDecl.getName().toString() + "Page");
            List<JCTree.JCVariableDecl> parameters = List.nil();
            JCTree.JCVariableDecl iPage = treeMaker.VarDef(
                    treeMaker.Modifiers(Flags.PARAMETER),
                    names.fromString("iPage"),
                    memberAccess("com.github.pagehelper.IPage"),
                    null
            );

            parameters = parameters.appendList(jcMethodDecl.getParameters());
            iPage.pos = jcMethodDecl.pos;
            parameters = parameters.append(iPage);
            List<JCTree.JCExpression> throwsClauses = jcMethodDecl.getThrows();

            JCTree.JCExpression exp1 = treeMaker.Apply(
                    List.nil(),
                    treeMaker.Select(
                            memberAccess("com.github.pagehelper.PageHelper"),
                            names.fromString("startPage")
                    ),
                    List.of(
                            treeMaker.Apply(
                                    List.nil(),
                                    treeMaker.Select(
                                            treeMaker.Ident(names.fromString("iPage")),
                                            names.fromString("getPageNum")
                                    ),
                                    List.nil()
                            ),
                            treeMaker.Apply(
                                    List.nil(),
                                    treeMaker.Select(
                                            treeMaker.Ident(names.fromString("iPage")),
                                            names.fromString("getPageSize")
                                    ),
                                    List.nil()
                            ),
                            treeMaker.Apply(
                                    List.nil(),
                                    treeMaker.Select(
                                            treeMaker.Ident(names.fromString("iPage")),
                                            names.fromString("getOrderBy")
                                    ),
                                    List.nil()
                            )
                    )
            );

            List<JCTree.JCExpression> params = List.nil();
            for (JCTree.JCVariableDecl jcVariableDecl : jcMethodDecl.getParameters()) {
                params = params.append(treeMaker.Ident(jcVariableDecl.name));
            }
            JCTree.JCExpression exp2 = treeMaker.Apply(
                    List.nil(),
                    treeMaker.Select(
                            treeMaker.Ident(names.fromString("this")),
                            jcMethodDecl.getName()
                    ),
                    params
            );

            List<JCTree.JCStatement> jcStatementList = List.nil();
            jcStatementList = jcStatementList.append(treeMaker.Exec(exp1));
            jcStatementList = jcStatementList.append(treeMaker.Return(exp2));
            JCTree.JCBlock block = treeMaker.Block(0, jcStatementList);

            return treeMaker.MethodDef(
                    modifiers,
                    name,
                    returnType,
                    List.nil(),
                    parameters,
                    throwsClauses,
                    block,
                    null
            );
        }

        private JCTree generateCount(JCTree.JCMethodDecl jcMethodDecl) {
            JCTree.JCModifiers modifiers = treeMaker.Modifiers(Flags.DEFAULT);
            JCTree.JCExpression returnType = treeMaker.TypeIdent(TypeTag.LONG);
            Name name = names.fromString(jcMethodDecl.getName().toString() + "Count");
            List<JCTree.JCVariableDecl> parameters = jcMethodDecl.getParameters();
            List<JCTree.JCExpression> throwsClauses = jcMethodDecl.getThrows();

            List<JCTree.JCExpression> params = List.nil();
            for (JCTree.JCVariableDecl jcVariableDecl : jcMethodDecl.getParameters()) {
                params = params.append(treeMaker.Ident(jcVariableDecl.name));
            }
            JCTree.JCExpression exp1 = treeMaker.Apply(
                    List.nil(),
                    treeMaker.Select(
                            memberAccess("com.github.pagehelper.PageHelper"),
                            names.fromString("count")
                    ),
                    List.of(treeMaker.Lambda(
                            List.nil(),
                            treeMaker.Apply(
                                    List.nil(),
                                    treeMaker.Select(
                                            treeMaker.Ident(names.fromString("this")),
                                            jcMethodDecl.getName()
                                    ),
                                    params
                            )
                    ))
            );

            List<JCTree.JCStatement> jcStatementList = List.nil();
            jcStatementList = jcStatementList.append(treeMaker.Return(exp1));
            JCTree.JCBlock block = treeMaker.Block(0, jcStatementList);

            return treeMaker.MethodDef(
                    modifiers,
                    name,
                    returnType,
                    List.nil(),
                    parameters,
                    throwsClauses,
                    block,
                    null
            );
        }
    }

    private JCTree.JCExpression memberAccess(String path) {
        String[] parts = path.split("\\.");
        JCTree.JCExpression expr = treeMaker.Ident(names.fromString(parts[0]));
        for (int i = 1; i < parts.length; i++) {
            expr = treeMaker.Select(expr, names.fromString(parts[i]));
        }
        return expr;
    }

}
