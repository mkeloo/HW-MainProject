package edu.ufl.cise.cop4020fa23;

import java.util.Stack;

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.exceptions.*;

public class TypeCheckVisitor implements ASTVisitor {

    private SymbolTable symbolTable;

//    helper string for context
    private static final String IN_LVALUE_CONTEXT = "IN_LVALUE_CONTEXT";


    // creatinf stack to store return types of functions/programs
    private Stack<Type> returnTypeStack = new Stack<>();

    public TypeCheckVisitor() throws TypeCheckException {
        this.symbolTable = new SymbolTable();
    }


    /* ======================= DANIEL ======================= */



    /* ======================= MOKSH ======================= */

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expr, Object arg) throws TypeCheckException, PLCCompilerException {
        Type redType = (Type) expr.getRed().visit(this, arg);
        Type greenType = (Type) expr.getGreen().visit(this, arg);
        Type blueType = (Type) expr.getBlue().visit(this, arg);

        if (redType != Type.INT || greenType != Type.INT || blueType != Type.INT) {
            throw new TypeCheckException("all components of an ExpandedPixelExpr must be of type INT");
        }

        return Type.PIXEL;
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws TypeCheckException, PLCCompilerException {
        Type widthType = (Type) dimension.getWidth().visit(this, arg);
        Type heightType = (Type) dimension.getHeight().visit(this, arg);

        if (widthType != Type.INT || heightType != Type.INT) {
            throw new TypeCheckException("width and height in dimension must be of type INT");
        }

        return Type.INT;
    }


    @Override
    public Object visitLValue(LValue lValue, Object arg) throws TypeCheckException, PLCCompilerException {
        NameDef nameDef = lValue.getNameDef();

        if (nameDef == null) {
            nameDef = symbolTable.lookup(lValue.getName());
            if (nameDef == null) {
                throw new TypeCheckException("LValue refers to an undefined name: " + lValue.getName());
            }
        }

        Type varType = nameDef.getType();

        PixelSelector pixelSelector = lValue.getPixelSelector();
        ChannelSelector channelSelector = lValue.getChannelSelector();

        if (pixelSelector != null && varType != Type.IMAGE) {
            throw new TypeCheckException("PixelSelector present, but LValue varType is not IMAGE. found: " + varType);
        }

        if (channelSelector != null && (varType != Type.PIXEL && varType != Type.IMAGE)) {
            throw new TypeCheckException("ChannelSelector present, but LValue varType is not PIXEL or IMAGE. found: " + varType);
        }

        if (pixelSelector == null && channelSelector == null) {
            lValue.setType(varType);
        } else if (varType == Type.IMAGE && pixelSelector != null && channelSelector == null) {
            lValue.setType(Type.PIXEL);
            if (arg instanceof LValue) {
                symbolTable.enterScope();
                symbolTable.insert(new SyntheticNameDef("x"));
                symbolTable.insert(new SyntheticNameDef("y"));
                pixelSelector.visit(this, arg);
                symbolTable.leaveScope();
            } else {
                pixelSelector.visit(this, arg);
            }
        } else if (varType == Type.IMAGE && pixelSelector != null && channelSelector != null) {
            lValue.setType(Type.INT);
            pixelSelector.visit(this, arg);
            channelSelector.visit(this, arg);
        } else if (varType == Type.IMAGE && pixelSelector == null && channelSelector != null) {
            lValue.setType(Type.INT);
            channelSelector.visit(this, arg);
        } else if (varType == Type.PIXEL && pixelSelector == null && channelSelector != null) {
            lValue.setType(Type.INT);
            channelSelector.visit(this, arg);
        } else {
            throw new TypeCheckException("Invalid combination in LValue.");
        }

        return lValue.getType();
    }



    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws TypeCheckException, PLCCompilerException {
//        System.out.println("Visiting Assignment Statement: " + assignmentStatement.getlValue().getName());
        LValue lValue = assignmentStatement.getlValue();
        Type lValueType;
        symbolTable.enterScope();
        if (lValue.getPixelSelector() != null) {
            lValueType = (Type) lValue.visit(this, IN_LVALUE_CONTEXT);
        } else {
            lValueType = (Type) lValue.visit(this, arg);
        }
        Type exprType = (Type) assignmentStatement.getE().visit(this, arg);
        symbolTable.leaveScope();
        if (!(lValueType == exprType
                || (lValueType == Type.PIXEL && exprType == Type.INT)
                || (lValueType == Type.IMAGE && (exprType == Type.PIXEL || exprType == Type.INT || exprType == Type.STRING)))) {
            throw new TypeCheckException("type mismatch in assignment. LValue type: " + lValueType + ", Expr type: " + exprType);
        }
        return exprType;
    }



    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws TypeCheckException, PLCCompilerException {
        Expr expr = writeStatement.getExpr();
        Type exprType = (Type) expr.visit(this, arg);
        if (exprType == null) {
            throw new TypeCheckException("type of the expression in WriteStatement has not been found yet.");
        }
        return exprType;
    }


    @Override
    public Object visitDoStatement(DoStatement doStatement, Object arg) throws TypeCheckException, PLCCompilerException {
        symbolTable.enterScope();
        try {
            for (GuardedBlock gBlock : doStatement.getGuardedBlocks()) {
                Type guardType = (Type) gBlock.getGuard().visit(this, arg);
                if (guardType != Type.BOOLEAN) {
                    throw new TypeCheckException("guard expression in DoStatement must be of type BOOLEAN");
                }
                gBlock.getBlock().visit(this, arg);
            }
        } finally {
            symbolTable.leaveScope();
        }
        return doStatement;
    }


    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws TypeCheckException, PLCCompilerException {
        for (GuardedBlock gBlock : ifStatement.getGuardedBlocks()) {
            Type guardType = (Type) gBlock.getGuard().visit(this, arg);
            if (guardType != Type.BOOLEAN) {
                throw new TypeCheckException("guard expression in IfStatement's GuardedBlock must be of type BOOLEAN");
            }
        }
        return ifStatement;
    }

    @Override
    public Object visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws TypeCheckException, PLCCompilerException {
        Type guardType = (Type) guardedBlock.getGuard().visit(this, arg);
        if (guardType != Type.BOOLEAN) {
            throw new TypeCheckException("guard expression in GuardedBlock must be of type BOOLEAN");
        }
        return guardType;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws TypeCheckException, PLCCompilerException {
        Expr returnedExpr = returnStatement.getE();
        Type returnedType = (Type) returnedExpr.visit(this, arg);
        if (returnTypeStack.isEmpty()) {
            throw new TypeCheckException("unexpected :( return statement outside of function or method scope.");
        }
        Type expectedReturnType = returnTypeStack.peek();
        if (returnedType != expectedReturnType) {
            throw new TypeCheckException("mismatched return type :(. Expected " + expectedReturnType + " but found " + returnedType);
        }
        return returnedType;
    }


    @Override
    public Object visitBlockStatement(StatementBlock statementBlock, Object arg) throws TypeCheckException, PLCCompilerException {
        statementBlock.getBlock().visit(this, arg);
        return statementBlock;
    }



}
