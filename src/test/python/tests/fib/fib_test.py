import cocotb
from cocotb.triggers import Timer
from cocotb.triggers import Edge


@cocotb.test()
async def run_fib(dut):
    """It should compute the fibonacci sequence"""
    # reset
    dut.reset.value = 1
    dut.io_go.value = 0
    await Timer(1, "ns")
    dut.reset.value = 0
    await Timer(1, "ns")
    # Take up go to start the circuit
    dut.io_go.value = 1

    fib = [1, 1, 2, 3, 5, 8, 13, 21, 34, 55]
    for f in fib:
        await Edge(dut.io_valid)
        await Timer(1, "ns") # Must wait a little bit before sampling output as it changes with valid
        assert dut.io_out.value == f

