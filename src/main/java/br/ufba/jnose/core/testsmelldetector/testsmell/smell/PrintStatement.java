package br.ufba.jnose.core.testsmelldetector.testsmell.smell;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import br.ufba.jnose.core.testsmelldetector.testsmell.AbstractSmell;
import br.ufba.jnose.core.testsmelldetector.testsmell.SmellyElement;
import br.ufba.jnose.core.testsmelldetector.testsmell.TestMethod;
import br.ufba.jnose.core.testsmelldetector.testsmell.Util;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/*
Test methods should not contain print statements as execution of unit tests is an automated process with little to no human intervention. Hence, print statements are redundant.
This code checks the body of each test method if System.out. print(), println(), printf() and write() methods are called
 */
public class PrintStatement extends AbstractSmell {

    public PrintStatement() {
        super("Print Statement");
    }

    /**
     * Analyze the test file for test methods that print output to the console
     */
    @Override
    public void runAnalysis(CompilationUnit testFileCompilationUnit, CompilationUnit productionFileCompilationUnit, String testFileName, String productionFileName) throws FileNotFoundException {
        classVisitor = new PrintStatement.ClassVisitor();
        classVisitor.visit(testFileCompilationUnit, null);
    }

    private class ClassVisitor extends VoidVisitorAdapter<Void> {
        private MethodDeclaration currentMethod = null;
        private int printCount = 0;
        TestMethod testMethod;

        // examine all methods in the test class
        @Override
        public void visit(MethodDeclaration n, Void arg) {
            if (Util.isValidTestMethod(n)) {
                currentMethod = n;
                testMethod = new TestMethod(n.getNameAsString());
                testMethod.setHasSmell(false); //default value is false (i.e. no smell)
                super.visit(n, arg);

                testMethod.addDataItem("begin",String.valueOf(n.getRange().get().begin.line));
                testMethod.addDataItem("end",String.valueOf(n.getRange().get().end.line));

                testMethod.setHasSmell(printCount >= 1);
                testMethod.addDataItem("PrintCount", String.valueOf(printCount));

                smellyElementList.add(testMethod);

                //reset values for next method
                currentMethod = null;
                printCount = 0;
            }
        }

        // examine the methods being called within the test method
        @Override
        public void visit(MethodCallExpr n, Void arg) {
            super.visit(n, arg);
            if (currentMethod != null) {
                // if the name of a method being called is 'print' or 'println' or 'printf' or 'write'
                if (n.getNameAsString().equals("print") || n.getNameAsString().equals("println") || n.getNameAsString().equals("printf") || n.getNameAsString().equals("write")) {
                    //check the scope of the method & proceed only if the scope is "out"
                    if ((n.getScope().isPresent() &&
                            n.getScope().get() instanceof FieldAccessExpr &&
                            (((FieldAccessExpr) n.getScope().get())).getNameAsString().equals("out"))) {

                        FieldAccessExpr f1 = (((FieldAccessExpr) n.getScope().get()));

                        //check the scope of the field & proceed only if the scope is "System"
                        if ((f1.getScope() != null &&
                                f1.getScope() instanceof NameExpr &&
                                ((NameExpr) f1.getScope()).getNameAsString().equals("System"))) {
                            //a print statement exists in the method body
                            printCount++;
                        }
                    }

                }
            }
        }

    }
}
