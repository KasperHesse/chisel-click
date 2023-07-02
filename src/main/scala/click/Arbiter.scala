package click

import chisel3._

/**
 * A merge-based arbiter, allowing two producers which are not mutually exclusive in their requests
 * to access a common consumer
 * @param typ The datatype on the inputs and output of the arbiter
 * @param conf
 * @tparam T
 */
class Arbiter[T <: Data](typ: T)(implicit conf: ClickConfig) extends Module with RequireAsyncReset {
  val io = IO(new Bundle {
    val in1 = new ReqAck(typ)
    val in2 = new ReqAck(typ)
    val out = Flipped(new ReqAck(typ))
  })

  val rgd = Module(new RGDMutex())
  val merge = Module(new Merge(typ))

  //inputs to RGD
  rgd.io.R1 := io.in1.req
  rgd.io.R2 := io.in2.req
  rgd.io.D1 := merge.io.in1.ack
  rgd.io.D2 := merge.io.in2.ack

  //Inputs to Merge
  merge.io.in1.req := rgd.io.G1
  merge.io.in2.req := rgd.io.G2
  merge.io.in1.data := io.in1.data
  merge.io.in2.data := io.in2.data

  //Module outputs
  io.out <> merge.io.out
  io.in1.ack := merge.io.in1.ack
  io.in2.ack := merge.io.in2.ack
}
