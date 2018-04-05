package net.cadrian.macchiato.recipe.ast.instruction;

public interface InstructionVisitor extends Assignment.Visitor, Block.Visitor, Emit.Visitor, If.Visitor, Next.Visitor,
		ProcedureCall.Visitor, While.Visitor {

}
