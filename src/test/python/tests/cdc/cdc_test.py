import cocotb
from cocotb.triggers import Timer
from cocotb.clock import Clock
from cocotb.triggers import Edge


@cocotb.test()
async def cross_clock_domain(dut):
    """It should perform clock-domain crossing"""
    # reset and clock start
    cocotb.start_soon(Clock(dut.clock, 10, "ns").start())
    dut.reset.value = 1
    dut.io_valid.value = 0

    # First values to transmit
    await Timer(15, "ns")
    dut.reset.value = 0
    dut.io_din.value = 0xab
    dut.io_valid.value = 1
    await Timer(10, "ns")
    dut.io_valid.value = 0
    await Timer(2000, "ns")
    assert dut.io_dout.value == 0xab

    # Second set of values to transmit
    dut.io_din.value = 0xf7
    dut.io_valid.value = 1
    await Timer(10, "ns")
    dut.io_valid.value = 0
    await Timer(2000, "ns")
    assert dut.io_dout.value == 0xf7

