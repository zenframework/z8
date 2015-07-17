package org.zenframework.z8.server.db.sql.expressions;

import org.zenframework.z8.server.resources.Resources;

public enum Operation {
    None(""),
    Not(Names.Not),
    Minus(Names.Minus),
    Add(Names.Add),
    Sub(Names.Sub),
    Mul(Names.Mul),
    Div(Names.Div),
    Mod(Names.Mod),
    And(Names.And),
    Or(Names.Or),
    Eq(Names.Eq),
    NotEq(Names.NotEq),
    LT(Names.LT),
    GT(Names.GT),
    LE(Names.LE),
    GE(Names.GE),

    BeginsWith(Names.BeginsWith),
    EndsWith(Names.EndsWith),
    Contains(Names.Contains),

    Group(Names.Group);

    class Names {
        static protected final String Not = "not";
        static protected final String Minus = "minus";
        static protected final String Add = "add";
        static protected final String Sub = "sub";
        static protected final String Mul = "mul";
        static protected final String Div = "div";
        static protected final String Mod = "mod";

        static protected final String And = "and";
        static protected final String Or = "or";
        static protected final String Eq = "eq";
        static protected final String NotEq = "notEq";
        static protected final String LT = "lt";
        static protected final String GT = "gt";
        static protected final String LE = "le";
        static protected final String GE = "ge";

        static protected final String Group = "group";

        static protected final String BeginsWith = "beginsWith";
        static protected final String EndsWith = "endsWith";
        static protected final String Contains = "contains";
    }

    private String fName = null;

    Operation(String name) {
        fName = name;
    }

    @Override
    public String toString() {
        return fName;
    }

    public String toReadableString() {
        switch(this) {
        case Not: return Resources.get("Operation.not");
        case Minus: return "-";
        case Add: return "+";
        case Sub: return "-";
        case Mul: return "*";
        case Div: return "/";
        case Mod: return "%";
        case And: return Resources.get("Operation.and");
        case Or: return Resources.get("Operation.or");
        case Eq: return "=";
        case NotEq: return "<>";
        case LT:return "<";
        case GT: return ">";
        case LE: return "<=";
        case GE: return ">=";
        case Group: return "";
        case BeginsWith: return Resources.get("Operation.beginsWith");
        case EndsWith: return Resources.get("Operation.endsWith");
        case Contains: return Resources.get("Operation.contains");
        default:
            throw new RuntimeException("Unknown operation: '" + fName + "'");
        }
    }

    static public Operation fromString(String string) {
        string = string.toLowerCase();

        if(Names.Not.toLowerCase().equals(string)) {
            return Operation.Not;
        }
        else if(Names.Minus.toLowerCase().equals(string)) {
            return Operation.Minus;
        }
        else if(Names.Add.toLowerCase().equals(string)) {
            return Operation.Add;
        }
        else if(Names.Sub.toLowerCase().equals(string)) {
            return Operation.Sub;
        }
        else if(Names.Mul.toLowerCase().equals(string)) {
            return Operation.Mul;
        }
        else if(Names.Div.toLowerCase().equals(string)) {
            return Operation.Div;
        }
        else if(Names.Mod.toLowerCase().equals(string)) {
            return Operation.Mod;
        }
        else if(Names.And.toLowerCase().equals(string)) {
            return Operation.And;
        }
        else if(Names.Or.toLowerCase().equals(string)) {
            return Operation.Or;
        }
        else if(Names.Eq.toLowerCase().equals(string)) {
            return Operation.Eq;
        }
        else if(Names.NotEq.toLowerCase().equals(string)) {
            return Operation.NotEq;
        }
        else if(Names.LT.toLowerCase().equals(string)) {
            return Operation.LT;
        }
        else if(Names.GT.toLowerCase().equals(string)) {
            return Operation.GT;
        }
        else if(Names.LE.toLowerCase().equals(string)) {
            return Operation.LE;
        }
        else if(Names.GE.toLowerCase().equals(string)) {
            return Operation.GE;
        }
        else if(Names.Group.toLowerCase().equals(string)) {
            return Operation.Group;
        }
        else if(Names.BeginsWith.toLowerCase().equals(string)) {
            return Operation.BeginsWith;
        }
        else if(Names.EndsWith.toLowerCase().equals(string)) {
            return Operation.EndsWith;
        }
        else if(Names.Contains.toLowerCase().equals(string)) {
            return Operation.Contains;
        }
        else {
            throw new RuntimeException("Unknown operation: '" + string + "'");
        }
    }
}
