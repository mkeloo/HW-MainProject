package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.NameDef;
import edu.ufl.cise.cop4020fa23.ast.Type;

public class Symbol {
    String name;
    int serialNumber;
    private NameDef nameDef;

    public Symbol(String name, int serialNumber, NameDef nameDef) {
        this.name = name;
        this.serialNumber = serialNumber;
        this.nameDef = nameDef;
    }

    public String getName() {
        return name;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public NameDef getNameDef() {
        return nameDef;
    }

    public Type getType() {
        return nameDef.getType();
    }
}
