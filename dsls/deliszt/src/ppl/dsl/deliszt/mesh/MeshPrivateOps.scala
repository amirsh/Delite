package ppl.dsl.deliszt.mesh

import java.io.PrintWriter

import ppl.delite.framework.DSLType
import scala.virtualization.lms.common._
import ppl.dsl.deliszt.{DeLisztExp, DeLiszt}
import ppl.dsl.deliszt.datastruct._
import ppl.dsl.deliszt.datastruct.scala._

/**
 * author: Michael Wu (mikemwu@stanford.edu)
 * last modified: 05/03/2011
 *
 * Pervasive Parallelism Laboratory (PPL)
 * Stanford University
 */

trait MeshPrivateOps extends DSLType with Variables {
  this: DeLiszt =>

  object MeshLoader {
    def init() = mesh_loader_init()
  }
  
  def mesh_loader_init() : Rep[Unit]

  def meshSet[MO<:MeshObj:Manifest] : Rep[MeshSet[MO]]
  def label[MO<:MeshObj:Manifest,VT:Manifest] : Rep[LabelField[MO,VT]]
}

trait MeshPrivateOpsExp extends VariablesExp with BaseFatExp {
  this: DeLisztExp =>

  case class MeshLoaderInit() extends Def[Unit]
  case class MeshSet[MO<:MeshObj]()(implicit moM : Manifest[MO]) extends Def[MeshSet[MO]]
  case class LabelField[MO<:MeshObj,VT]()(implicit moM : Manifest[MO], vtM: Manifest[VT]) extends Def[LabelField[MO,VT]]

  def mesh_loader_init() = MeshLoaderInit()
  def meshSet[MO<:MeshObj:Manifest] = MeshSet[MO]
  def label[MO<:MeshObj:Manifest,VT:Manifest] = LabelField[MO,VT]
}

trait ScalaGenVecPrivateOps extends ScalaGenBase {
  val IR: MeshPrivateOpsExp
  import IR._

  override def emitNode(sym: Sym[Any], rhs: Def[Any])(implicit stream: PrintWriter) = {
    rhs match {
      case MeshLoaderInit() => emitValDef(sym, "generated.scala.datastruct.MeshLoader.init()")
      case ms@MeshSet() => emitValDef(sym, "generated.scala.datastruct.Mesh.mesh.meshSet[" + remap(ms.moM) + "]")
      case lf@LabelField() => emitValDef(sym, "generated.scala.datastruct.Mesh.mesh.label[" + remap(ms.moM) + "," + remap(ms.vtM) + "]")
      case _ => super.emitNode(sym, rhs)
    }
  }
}


trait CudaGenVecPrivateOps extends CudaGenBase with CudaGenDataStruct {
  val IR: MeshPrivateOpsExp
  import IR._

  override def emitNode(sym: Sym[Any], rhs: Def[Any])(implicit stream: PrintWriter) = rhs match {
    case _ => super.emitNode(sym, rhs)
  }
}

trait CGenVecPrivateOps extends CGenBase {
  val IR: MeshPrivateOpsExp
  import IR._

  override def emitNode(sym: Sym[Any], rhs: Def[Any])(implicit stream: PrintWriter) = rhs match {
    case _ => super.emitNode(sym, rhs)
  }
}