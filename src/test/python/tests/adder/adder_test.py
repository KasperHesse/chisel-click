import cocotb
from cocotb.triggers import Timer
from cocotb.triggers import Edge


@cocotb.test()
async def add_numbers(dut):
    """It should add two numbers combinationally"""
    # reset
    dut.reset.value = 1
    dut.io_in1_req.value = 0
    dut.io_in2_req.value = 0
    dut.io_in1_data.value = 42
    dut.io_in2_data.value = 84
    dut.io_out_ack.value = 0
    await Timer(5, "ns")
    dut.reset.value = 0
    await Timer(1, "ns")

    assert dut.io_out_req.value == 0
    dut.io_in1_req.value = 1
    dut.io_in2_req.value = 1
    await Edge(dut.io_out_req)

    assert dut.io_out_req.value == 1
    assert dut.io_out_data.value == (42+84)
    dut.io_out_ack.value = 1
    await Timer(2, "ns")
