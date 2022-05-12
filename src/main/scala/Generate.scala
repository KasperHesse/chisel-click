import chisel3._
import chisel3.util.Cat
import chisel3.stage.ChiselStage
import click._
import examples._

import java.io.{BufferedWriter, File, FileWriter, RandomAccessFile}
import scala.io.Source

/**
 * Calling this class will generate all of the Verilog files necessary for testing with
 * cocotb and Icarus Verilog. The make target `make gen` will call this and generate the files
 */
object Generate extends App {
  def addVcd(name: String): Unit = {
    val f = new RandomAccessFile(s"gen/$name.v", "rw")
    var pos = f.length()-5
    f.seek(pos)

    //Seek backwards till we hit first newline
    while(f.read() != '\n'.toInt) {
      pos -= 1
      f.seek(pos)
    }
    f.writeBytes(s"""`ifdef COCOTB_SIM
                    |initial begin
                    |  $$dumpfile("dump.vcd");
                    |  $$dumpvars(0, $name);
                    |  #1;
                    |end
                    |`endif
                    |endmodule""".stripMargin)
    f.close()
  }

  def renameModule(oldName: String, newName: String): Unit = {
    val src = Source.fromFile(s"gen/$oldName.v")
    val bw = new BufferedWriter(new FileWriter(s"gen/$newName.v"))
    src.getLines().map{l =>
      if (l.contains(s"module $oldName(")) s"module $newName(" else l
    }.foreach(l => bw.write(s"$l\n"))
    src.close()
    bw.close()
    new File(s"gen/$oldName.v").deleteOnExit()
  }

  def gen(x: => RawModule): Unit = {

    val params = Array("-td", "gen", "--emission-options", "disableRegisterRandomization")
    val cs = new ChiselStage
    cs.emitVerilog(x, params)
  }

  val cc = ClickConfig()

  //GENERATE FILES
  gen(Adder(8)(cc))
  gen(new CDC()(cc))
  gen(new Demultiplexer(UInt(8.W))(cc))
  gen(new Fib(8)(cc))
  gen(Fifo(5, 0.U(8.W), false)(cc))
  gen(Fork(UInt(8.W))(cc))
  gen(new Fib(8)(cc))
  gen(new GCD(8)(cc))
  gen(Join(8)(cc))
  gen(JoinReg(8, 4, ro = true)(cc))
  //Simple join-reg-fork block
  gen(JoinRegFork(widthIn=8, valueOut=0, ro=false)(cc))
  renameModule("JoinRegFork", "JRF_simple")
  //The complex JRF uses different phases on the output ports and performs bit-moving between the inputs and outputs
  gen(new JoinRegFork(UInt(8.W), UInt(10.W), 0.U(4.W), 4.U(14.W), false, true, (a: UInt, b: UInt) => {
    val c = Cat(a, b)
    (c(17,14), c(13, 0))
  })(cc))
  renameModule("JoinRegFork", "JRF_complex")

  gen(new Merge(UInt(8.W))(cc))
  gen(new Multiplexer(UInt(8.W))(cc))
  gen(RegFork(4.U(8.W), false)(cc))


  //Add VCD footers for simulation
  addVcd("Adder")
  addVcd("CDC")
  addVcd("Demultiplexer")
  addVcd("Fib")
  addVcd("Fifo")
  addVcd("GCD")
  addVcd("Join")
  addVcd("JoinReg")
  addVcd("JRF_complex")
  addVcd("JRF_simple")
  addVcd("Merge")
  addVcd("Multiplexer")
  addVcd("RegFork")
}
