package examples

import chisel3._
import chisel3.stage.ChiselStage
import chisel3.util._
import click._

/**
 * An example of clock-domain crossing using the asynchronous click elements
 * This example uses a 4-deep asynchronous FIFO to transfer data between a producer and a consumer
 */
class CDC(implicit conf: ClickConfig) extends Module {
  val io = IO(new Bundle {
    val din = Input(UInt(8.W))
    val valid = Input(Bool())
    val dout = Output(UInt(8.W))
  })

  //Generate rising-edge detecting valid signal
  val valid = RegNext(RegNext(io.valid))
  val v = valid && !RegNext(valid)

  //Generate downsampled clock signal
  val clk2 = RegInit(false.B)
  val clkCnt = RegInit(0.U(2.W))
  clkCnt := clkCnt + 1.U
  clk2 := Mux(clkCnt === 3.U, !clk2, clk2)

  val prod = Module(new Producer)
  val fifo = Module(new Fifo(4, 1)(conf))
  val cons = withClock(clk2.asClock) {Module(new Consumer)}

  prod.io.din := io.din
  prod.io.valid := v
  prod.io.out <> fifo.io.in
  fifo.io.out <> cons.io.in
  fifo.io.reset := this.reset.asAsyncReset
  io.dout := cons.io.dout


}

/**
 * The producer is a parallel-to-serial converter, transmitting a byte over the asynchronous link
 */
class Producer extends Module {
  val io = IO(new Bundle {
    val din = Input(UInt(8.W))
    val valid = Input(Bool())
    val out = Flipped(new ReqAck(Bool()))
  })

  val sIdle :: sTransmit :: Nil = Enum(2)
  val state = RegInit(sIdle)
  val buf = RegInit(0.U(8.W))
  val cnt = RegInit(0.U(4.W))
  val req = RegInit(false.B) //Output request signal
  val ack = RegNext(RegNext(io.out.ack)) //Using 2 regs to let metastability settle
  val ackEdge = ack =/= RegNext(ack)

  switch(state) {
    is(sIdle) {
      when(io.valid) {
        buf := io.din
        state := sTransmit
        cnt := 0.U
        req := !req
      }
    }

    is(sTransmit) {
      when(ackEdge && cnt < 7.U) {
        cnt := cnt + 1.U
        req := !req
        buf := (buf >> 1)
      } .elsewhen (ackEdge) {
        state := sIdle
      }
    }
  }

  io.out.req := req
  io.out.data := buf(0)
}

/**
 * The consumer is a serial-to-parallel converter, receiving a byte over the asynchronous link
 */
class Consumer extends Module {
  val io = IO(new Bundle {
    val in = new ReqAck(Bool())
    val dout = Output(UInt(8.W))
  })

  val r = withReset(this.reset.asAsyncReset) {RegNext(io.in.req, false.B)}
  val req = withReset(this.reset.asAsyncReset) {RegNext(r, false.B)}
  val buf = withReset(this.reset.asAsyncReset) {RegInit(0.U(8.W))}
  val reqEdge = req =/= RegNext(req)
  val ack = withReset(this.reset.asAsyncReset) {RegInit(false.B)}

  when(reqEdge) {
    ack := !ack
    buf := Cat(io.in.data, buf(7,1))
  }
  io.in.ack := ack
  io.dout := buf
}

object CDC extends App {
  val conf = ClickConfig(SIMULATION = false)
  (new ChiselStage).emitVerilog(new CDC()(conf))
}
