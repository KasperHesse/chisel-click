import cocotb
from cocotb.triggers import Timer
from cocotb.triggers import RisingEdge, FallingEdge


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
    await Timer(1, "ns")

    # Raise requests in an ordered fashion
    dut.io_in1_req.value = 1
    await Timer(1, "ns")
    assert dut.io_out_req.value == 0
    dut.io_in2_req.value = 1
    await Timer(1, "ns")
    assert dut.io_out_req.value == 1
    assert dut.io_out_data.value == (84 << 8) | 42

    await Timer(1, "ns")
    dut.io_out_ack.value = 1
    await Timer(1, "ns")
    assert dut.io_in1_ack.value == 1
    assert dut.io_in2_ack.value == 1
    await Timer(1, "ns")

    dut.io_in2_req.value = 0
    assert dut.io_out_req.value == 1
    dut.io_in1_req.value = 0
    await Timer(1)
    assert dut.io_out_req.value == 0
