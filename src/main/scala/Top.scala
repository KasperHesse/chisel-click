import chisel3._
import chisel3.util._
import chisel3.stage.ChiselStage
import click._
import examples._

import scala.io._
import java.io.{BufferedOutputStream, BufferedWriter, File, FileWriter, RandomAccessFile}

object Top extends App {

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

  val conf = ClickConfig()
  (new ChiselStage).emitVerilog(new CDC()(conf), Array("-td", "gen", "--emission-options", "disableRegisterRandomization"))
  addVcd("CDC")
}