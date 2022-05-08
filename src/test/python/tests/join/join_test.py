import cocotb
from cocotb.triggers import Timer
from cocotb.triggers import Edge


@cocotb.test()
async def join_data(dut):
    """It should join some data on the input channel"""
    # Reset
    dut.reset.value = 1
    dut.io_in1_req.value = 0
    dut.io_in2_req.value = 0
    dut.io_in1_data.value = 42
    dut.io_in2_data.value = 84
    dut.io_out_ack.value = 0
    await Timer(1, "ns")
    dut.reset.value = 0
    await Timer(5, "ns")

    # Should not propagate req when only in1 is high
    dut.io_in1_req.value = 1
    await Timer(10, "ns")
    assert dut.io_out_req.value == 0

    # Should not propagate when only in2 is high
    dut.io_in1_req.value = 0
    await Timer(1)
    dut.io_in2_req.value = 1
    await Timer(10, "ns")
    assert dut.io_out_req.value == 0

    # Should propagate when both are high
    dut.io_in1_req.value = 1
    await Edge(dut.io_out_req)
    await Timer(1)
    assert dut.io_out_req.value == 1
    assert dut.io_out_data.value == (84 << 8) | 42
