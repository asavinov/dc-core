 /*
 * Copyright 2013-2015 Alexandr Savinov
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

package com.conceptoriented.dce;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.tree.ParseTree;

import com.conceptoriented.dce.ExprBaseVisitor;
import com.conceptoriented.dce.ExprLexer;
import com.conceptoriented.dce.ExprParser;

public class ExprBuilder extends ExprBaseVisitor<ExprNode> {

    static boolean accessAsThisNode = true; // Design alternative: access node can be represented either as a child or this node

    //
    // Visitor interface
    //

    @Override
    public ExprNode visitExpr(ExprParser.ExprContext context)
    {
        ExprNode n = new ExprNode();

        // Determine the type of expression

        if (context.op != null && context.op.getText().equals(".")) // Composition (dot) operation
        {
            n.setOperation(OperationType.CALL);
            n.setAction(ActionType.READ);

            ExprNode exprNode = visit(context.expr(0));
            if (exprNode != null)
            {
                n.addChild(exprNode);
            }

            ExprNode accessNode = visit(context.access());
            if (accessAsThisNode) // Represent accessor (after dot) by this node
            {
                if (context.access().name() != null) // Name of the function
                {
                    n.setName(accessNode.getName());
                }
                else // A definition of the function (lambda) is provided instead of name
                {
                    context.access().scope();
                    n.setName("lambda"); // Automatically generated name for a unnamed lambda
                }
            }
            else // Access node as a child (it can be named either by real method name or as a special 'method' with the method described represented elsewhere)
            {
                n.setName(".");
                if (accessNode != null)
                {
                    n.addChild(accessNode);
                }
            }
        }
        else if (context.op != null) // Arithmetic operations
        {
            n.setOperation(OperationType.CALL);
            String op = context.op.getText(); // Alternatively, context.GetChild(1).GetText()
            n.setName(op);

            if (op.equals("*")) n.setAction(ActionType.MUL);
            else if (op.equals("/")) n.setAction(ActionType.DIV);
            else if (op.equals("+")) n.setAction(ActionType.ADD);
            else if (op.equals("-")) n.setAction(ActionType.SUB);

            else if (op.equals("<=")) n.setAction(ActionType.LEQ);
            else if (op.equals(">=")) n.setAction(ActionType.GEQ);
            else if (op.equals(">")) n.setAction(ActionType.GRE);
            else if (op.equals("<")) n.setAction(ActionType.LES);

            else if (op.equals("==")) n.setAction(ActionType.EQ);
            else if (op.equals("!=")) n.setAction(ActionType.NEQ);

            else if (op.equals("&&")) n.setAction(ActionType.AND);
            else if (op.equals("||")) n.setAction(ActionType.OR);

            else ;

            ExprNode expr1 = visit(context.getChild(0));
            if (expr1 != null)
            {
                n.addChild(expr1);
            }

            ExprNode expr2 = visit(context.getChild(2));
            if (expr2 != null)
            {
                n.addChild(expr2);
            }
        }
        else if (context.expr() != null && context.getChild(0).getText().equals("(")) // Priority
        {
            n = visit(context.expr(0)); // Skip
        }
        else if (context.getChild(0).getText().equals("call:")) // Native method call
        {
            n.setOperation(OperationType.CALL);
            n.setAction(ActionType.PROCEDURE);

            String className = context.className.getText();
            n.setNameSpace(className); // Non-empty name space is an indication of a native method

            String methodName = context.methodName.getText();
            n.setName(methodName);

            int argCount = context.expr().size();
            for (int i = 0; i < argCount; i++)
            {
                ExprNode arg = visit(context.expr(i));
                if (arg != null)
                {
                    n.addChild(arg);
                }
            }
        }
        else if (context.getChild(0).getText().equals("((") || context.getChild(0).getText().equals("TUPLE")) // Tuple
        {
            n.setOperation(OperationType.TUPLE);
            n.setAction(ActionType.READ); // Find

            // Find all members and store them in the tuple node
            int mmbrCount = context.member().size();
            for (int i = 0; i < mmbrCount; i++)
            {
                ExprNode mmbr = visit(context.member(i));
                if (mmbr != null)
                {
                    n.addChild(mmbr);
                }
            }
        }
        else if (context.literal() != null) // Literal
        {
            n.setOperation(OperationType.VALUE);
            n.setAction(ActionType.READ);

            String name = context.literal().getText();

            if (context.literal().INT() != null)
            {
            }
            else if (context.literal().DECIMAL() != null)
            {
            }
            else if (context.literal().STRING() != null)
            {
                name = name.substring(1, name.length() - 1); // Remove quotes
            }
            n.setName(name);
        }
        else if (context.getChild(0) instanceof ExprParser.AccessContext) // Access/call
        {
            n.setOperation(OperationType.CALL);
            n.setAction(ActionType.READ);

            if (context.access().name() != null) // Name of the function - call by-reference
            {
                n.setName(GetName(context.access().name()));
            }
            else // Call by-value using a definition of the function (lambda)
            {
                context.access().scope();
                n.setName("lambda"); // Automatically generated name for a unnamed lambda
            }

            // Find all parameters and store them in the access node
            int paramCount = context.access().param().size();
            for (int i = 0; i < paramCount; i++)
            {
                ExprNode param = visit(context.access().param(i));
                if (param != null)
                {
                    n.addChild(param);
                }
            }
        }

        return n;
    }

    @Override
    public ExprNode visitParam(ExprParser.ParamContext context)
    {
        ExprNode n = new ExprNode();

        // Determine declared member (constituent, offset, parameter) name
        String name = GetName(context.name());

        // Determine value assigned to this param (it can be a CALL node, TUPLE node etc.)
        ExprNode expr = null;
        if (context.expr() != null)
        {
            expr = visit(context.expr());
        }
        else if (context.scope() != null)
        {
            throw new UnsupportedOperationException("Scopes in method parameters are currently not implemented");
        }

        n.addChild(expr);

        n.setName(name);
        n.setOperation(OperationType.TUPLE);
        n.setAction(ActionType.READ);

        return n;
    }

    @Override
    public ExprNode visitMember(ExprParser.MemberContext context)
    {
        // Determine declared (output, returned) type of the member
        String type;
        if (context.type().DELIMITED_ID() != null)
        {
            type = context.type().DELIMITED_ID().getText();
            type = type.substring(1, type.length() - 1); // Remove delimiters
        }
        else
        {
            type = context.type().ID().getText();
        }

        // Determine declared member (constituent, offset, parameter) name
        String name = GetName(context.name());

        // Determine value assigned to this member (it can be a CALL node, TUPLE node etc.)
        ExprNode expr = null;
        if (context.expr() != null)
        {
            expr = visit(context.expr());
        }
        else if (context.scope() != null)
        {
            throw new UnsupportedOperationException("Scopes in tuple members are currently not implemented");
        }

        ExprNode n;
        if (expr.getOperation() == OperationType.TUPLE) // Use directly this TUPLE node as a member node
        {
            n = expr;
        }
        else // Create a (primitive, leaf) TUPLE node with the only child as an expression
        {
            n = new ExprNode();
            n.addChild(expr);
        }

        n.setName(name);
        n.getResult().setTypeName(type);
        n.setOperation(OperationType.TUPLE);
        n.setAction(ActionType.READ);

        return n;
    }

    @Override
    public ExprNode visitName(ExprParser.NameContext context)
    {
        ExprNode n = new ExprNode();
        n.setOperation(OperationType.VALUE);
        n.setAction(ActionType.READ);

        n.setName(GetName(context));

        return n;
    }

    protected String GetName(ExprParser.NameContext context)
    {
        String name;
        if (context.DELIMITED_ID() != null)
        {
            name = context.DELIMITED_ID().getText();
            name = name.substring(1, name.length() - 1); // Remove delimiters
        }
        else
        {
            name = context.ID().getText();
        }

        return name;
    }

    public ExprNode build(String str)
    {
        ExprBuilder builder = this;

        ExprLexer lexer;
        ExprParser parser;
        ParseTree tree;
        String tree_str;
        ExprNode ast;

        lexer = new ExprLexer(new ANTLRInputStream(str));
        parser = new ExprParser(new CommonTokenStream(lexer));
        tree = parser.expr();
        tree_str = tree.toStringTree(parser);

        ast = builder.visit(tree);

        return ast;
    }
}
