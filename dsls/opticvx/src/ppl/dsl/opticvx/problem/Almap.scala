// Almap = Abstract Linear MAP
// This represents a linear map as a composition of a set of linear mapping primitives

package ppl.dsl.opticvx.problem

import scala.collection.immutable.Seq

trait Almap extends HasArity[Almap] {
  //The shape of the input parameters used by this map
  val input: Shape
  //The domain and codomain of this map
  val domain: Shape
  val codomain: Shape

  //Constraints that all the shape properties must share the map's arity
  if (input.arity != arity) throw new ProblemIRValidationException()
  if (domain.arity != arity) throw new ProblemIRValidationException()
  if (codomain.arity != arity) throw new ProblemIRValidationException()
  
  //The transpose
  def T: Almap

  //Matrix multiply
  //def mmpy(m: Almap): Almap
}

//The identity map from scalars to scalars
case class AlmapIdentity(val input: Shape, val domain: Shape) extends Almap {
  val arity: Int = input.arity
  val codomain: Shape = domain
  
  def arityOp(op: ArityOp): Almap = AlmapIdentity(input.arityOp(op), domain.arityOp(op))

  def T: Almap = this

  // def mmpy(m: Almap): Almap = {
  //   if (m.arity != arity) throw new ProblemIRValidationException()
  //   if (m.input != input) throw new ProblemIRValidationException()
  //   if (m.codomain != domain) throw new ProblemIRValidationException()
  //   m
  // }
}

//The zero map
case class AlmapZero(val input: Shape, val domain: Shape, val codomain: Shape) extends Almap {
  val arity: Int = input.arity
  
  def arityOp(op: ArityOp): Almap = AlmapZero(input.arityOp(op), domain.arityOp(op), codomain.arityOp(op))

  def T: Almap = AlmapZero(input, codomain, domain)

  // def mmpy(m: Almap): Almap = {
  //   if (m.arity != arity) throw new ProblemIRValidationException()
  //   if (m.input != input) throw new ProblemIRValidationException()
  //   if (m.codomain != domain) throw new ProblemIRValidationException()
  //   AlmapZero(input, m.domain, codomain)
  // }
}

//The sum of several linear maps
case class AlmapSum(val args: Seq[Almap]) extends Almap {
  val arity: Int = args(0).arity
  val input: Shape = args(0).input
  val domain: Shape = args(0).domain
  val codomain: Shape = args(0).codomain

  for(a <- args) {
    if (a.arity != arity) throw new ProblemIRValidationException()
    if (a.input != input) throw new ProblemIRValidationException()
    if (a.domain != domain) throw new ProblemIRValidationException()
    if (a.codomain != codomain) throw new ProblemIRValidationException()
  }

  def arityOp(op: ArityOp): Almap = AlmapSum(args map ((x) => x.arityOp(op)))

  def T: Almap = AlmapSum(args map ((x) => x.T))

  // def mmpy(m: Almap): Almap = AlmapSum(args map ((a) => a.mmpy(m)))
}

//Negation of a linear map
case class AlmapNeg(val arg: Almap) extends Almap {
  val arity: Int = arg.arity
  val input: Shape = arg.input
  val domain: Shape = arg.domain
  val codomain: Shape = arg.codomain
  
  def arityOp(op: ArityOp): Almap = AlmapNeg(arg.arityOp(op))

  def T: Almap = AlmapNeg(arg.T)

  // def mmpy(m: Almap): Almap = AlmapNeg(arg.mmpy(m))
}

//Scale of a linear map by a parameter that is itself a linear functional from the input space
case class AlmapScale(val arg: Almap, val scale: Almap) extends Almap {
  val arity: Int = arg.arity
  val input: Shape = arg.input
  val domain: Shape = arg.domain
  val codomain: Shape = arg.codomain

  if (arity != scale.arity) throw new ProblemIRValidationException()
  if (input != scale.input) throw new ProblemIRValidationException()
  if (input != scale.domain) throw new ProblemIRValidationException()
  if (!(scale.codomain.isInstanceOf[ShapeScalar])) throw new ProblemIRValidationException()
  
  def arityOp(op: ArityOp): Almap = AlmapScale(arg.arityOp(op), scale.arityOp(op))
  
  def T: Almap = AlmapScale(arg.T, scale)

  // def mmpy(m: Almap): Almap = AlmapScale(arg.mmpy(m), scale)
}

//Scale of a linear map by a constant
case class AlmapScaleConstant(val arg: Almap, val scale: Float) extends Almap {
  val arity: Int = arg.arity
  val input: Shape = arg.input
  val domain: Shape = arg.domain
  val codomain: Shape = arg.codomain
  
  def arityOp(op: ArityOp): Almap = AlmapScaleConstant(arg.arityOp(op), scale)
  
  def T: Almap = AlmapScaleConstant(arg.T, scale)

  // def mmpy(m: Almap): Almap = AlmapScaleConstant(arg.mmpy(m), scale)
}


//The vertical concatenation of several linear maps
case class AlmapVCat(val args: Seq[Almap]) extends Almap {
  val arity: Int = args(0).arity
  val input: Shape = args(0).input
  val domain: Shape = args(0).domain
  val codomain: Shape = ShapeStruct(args map ((a) => a.codomain))

  for(a <- args) {
    if (a.arity != arity) throw new ProblemIRValidationException()
    if (a.input != input) throw new ProblemIRValidationException()
    if (a.domain != domain) throw new ProblemIRValidationException()
  }

  def arityOp(op: ArityOp): Almap = AlmapVCat(args map ((a) => a.arityOp(op)))

  def T: Almap = AlmapHCat(args map ((a) => a.T))

  // def mmpy(m: Almap): Almap = AlmapVCat(args map ((a) => a.mmpy(m)))
}

//The vertical concatenation of a number of linear maps, depending on problem size
case class AlmapVCatFor(val size: Size, val arg: Almap) extends Almap {
  val arity: Int = size.arity
  val input: Shape = arg.input.demote
  val domain: Shape = arg.domain.demote
  val codomain: Shape = ShapeFor(size, arg.codomain)

  if (arg.arity != (size.arity + 1)) throw new ProblemIRValidationException()

  def arityOp(op: ArityOp): Almap = AlmapVCatFor(size.arityOp(op), arg.arityOp(op))

  def T: Almap = AlmapHCatFor(size, arg.T)

  // def mmpy(m: Almap): Almap = AlmapVCatFor(size, arg.mmpy(m.promote))
}


//The horizontal concatenation of several linear maps
case class AlmapHCat(val args: Seq[Almap]) extends Almap {
  val arity: Int = args(0).arity
  val input: Shape = args(0).input
  val domain: Shape = ShapeStruct(args map ((a) => a.domain))
  val codomain: Shape = args(0).codomain

  for(a <- args) {
    if (a.arity != arity) throw new ProblemIRValidationException()
    if (a.input != input) throw new ProblemIRValidationException()
    if (a.codomain != codomain) throw new ProblemIRValidationException()
  }

  def arityOp(op: ArityOp): Almap = AlmapHCat(args map ((a) => a.arityOp(op)))

  def T: Almap = AlmapVCat(args map ((a) => a.T))

  // def mmpy(m: Almap): Almap = m match {
  //   case AlmapVCat(ma) => AlmapSum(for(i <- 0 until args.length) yield args(i).mmpy(ma(i)))
  //   case _ => throw new ProblemIRValidationException()
  // }
}

//The horizontal concatenation of a number of linear maps, depending on problem size
case class AlmapHCatFor(val size: Size, val arg: Almap) extends Almap {
  val arity: Int = size.arity
  val input: Shape = arg.input.demote
  val domain: Shape = ShapeFor(size, arg.domain)
  val codomain: Shape = arg.codomain.demote

  if (arg.arity != (size.arity + 1)) throw new ProblemIRValidationException()

  def arityOp(op: ArityOp): Almap = AlmapHCatFor(size.arityOp(op), arg.arityOp(op))

  def T: Almap = AlmapVCatFor(size, arg.T)

  // def mmpy(m: Almap): Almap = m match {
  //   case AlmapVCatFor(s, ma) => {
  //     if (s != size) throw new ProblemIRValidationException()
  //     AlmapSumFor(size, arg.mmpy(ma))
  //   }
  //   case _ => throw new ProblemIRValidationException()
  // }
}

//The sum of a problem-size-dependent number of linear ops
case class AlmapSumFor(val size: Size, val arg: Almap) extends Almap {
  val arity: Int = size.arity
  val input: Shape = arg.input.demote
  val domain: Shape = arg.domain.demote
  val codomain: Shape = arg.codomain.demote

  if (arg.arity != (size.arity + 1)) throw new ProblemIRValidationException()

  def arityOp(op: ArityOp): Almap = AlmapSumFor(size.arityOp(op), arg.arityOp(op))

  def T: Almap = AlmapSumFor(size, arg.T)

  // def mmpy(m: Almap): Almap = AlmapSumFor(size, arg.mmpy(m.promote))
}

//A delta function based on the integer variables.  Takes on the argument if pred == 0, and zero otherwise.
case class AlmapIf(val pred: Size, val arg: Almap) extends Almap {
  val arity: Int = arg.arity
  val input: Shape = arg.input
  val domain: Shape = arg.domain
  val codomain: Shape = arg.codomain

  if (pred.arity != arity) throw new ProblemIRValidationException()

  def arityOp(op: ArityOp): Almap = AlmapIf(pred.arityOp(op), arg.arityOp(op))

  def T: Almap = AlmapIf(pred, arg.T)

  // def mmpy(m: Almap): Almap = AlmapIf(pred, arg.mmpy(m))
}

//Matrix multiply
case class AlmapProd(val argl: Almap, val argr: Almap) extends Almap {
  if (argl.arity != argr.arity) throw new ProblemIRValidationException()
  if (argl.input != argr.input) throw new ProblemIRValidationException()
  if (argl.domain != argr.codomain) throw new ProblemIRValidationException()

  val arity: Int = argl.arity
  val input: Shape = argl.input
  val domain: Shape = argr.domain
  val codomain: Shape = argl.codomain

  def arityOp(op: ArityOp): Almap = AlmapProd(argl.arityOp(op), argr.arityOp(op))

  def T: Almap = AlmapProd(argr.T, argl.T)
}

/*
object AlmapUtil {
  def sum(x: Almap, y: Almap): Almap = {
    if (x.arity != y.arity) throw new ProblemIRValidationException()
    if (x.input != y.input) throw new ProblemIRValidationException()
    if (x.domain != y.domain) throw new ProblemIRValidationException()
    if (x.codomain != y.codomain) throw new ProblemIRValidationException()
    x.codomain match {
      case ShapeScalar(ar) => AlfSum(x, y)
      case ShapeFor(sz, b) => 
        AlmapFor(sz, sum(
          AlmapIndex(Size.param(x.arity, x.arity + 1), x.promote),
          AlmapIndex(Size.param(x.arity, x.arity + 1), y.promote)))
      case ShapeStruct(bs) =>
        AlmapStruct(for (i <- 0 until bs.length) yield
          sum(AlmapAccess(i, x), AlmapAccess(i, y)))
    }
  }
  
  def neg(x: Almap): Almap = {
    x.codomain match {
      case ShapeScalar(ar) => AlfNeg(x)
      case ShapeFor(sz, b) =>
        AlmapFor(sz, neg(AlmapIndex(Size.param(x.arity, x.arity + 1), x.promote)))
      case ShapeStruct(bs) =>
        AlmapStruct(for (i <- 0 until bs.length) yield neg(AlmapAccess(i, x)))
    }
  }
  
  def scale(x: Almap, c: Almap): Almap = {
    if (x.arity != c.arity) throw new ProblemIRValidationException()
    if (x.input != c.input) throw new ProblemIRValidationException()
    if (x.input != c.domain) throw new ProblemIRValidationException()
    if (!(c.codomain.isInstanceOf[ShapeScalar])) throw new ProblemIRValidationException()
    x.codomain match {
      case ShapeScalar(ar) => AlfScale(x, c)
      case ShapeFor(sz, b) =>
        AlmapFor(sz, scale(AlmapIndex(Size.param(x.arity, x.arity + 1), x.promote), c))
      case ShapeStruct(bs) =>
        AlmapStruct(for (i <- 0 until bs.length) yield scale(AlmapAccess(i, x), c))
    }
  }
  
  def scaleconstant(x: Almap, c: Float): Almap = {
    x.codomain match {
      case ShapeScalar(ar) => AlfScaleConstant(x, c)
      case ShapeFor(sz, b) =>
        AlmapFor(sz, scaleconstant(AlmapIndex(Size.param(x.arity, x.arity + 1), x.promote), c))
      case ShapeStruct(bs) =>
        AlmapStruct(for (i <- 0 until bs.length) yield scaleconstant(AlmapAccess(i, x), c))
    }
  }
  
  def reduce(s: Size, x: Almap): Almap = {
    if (x.arity != (s.arity + 1)) throw new ProblemIRValidationException()
    x.codomain match {
      case ShapeScalar(ar) => AlfReduce(s, x)
      case ShapeFor(sz, b) =>
        AlmapFor(sz, reduce(s, 
          AlmapIndex(Size.param(s.arity, s.arity + 1), x.addParam(s.arity))))
      case ShapeStruct(bs) =>
        AlmapStruct(for (i <- 0 until bs.length) yield reduce(s, AlmapAccess(i, x)))
    }
  }
}
*/

