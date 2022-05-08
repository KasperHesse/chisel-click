import cocotb
from cocotb.triggers import Timer
from cocotb.triggers import Edge


@cocotb.test()
async def merge_data(dut):
    """It should merge two data channels that are mutually exclusive"""
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

    assert dut.io_out_req.value == 0
    assert dut.io_in1_ack.value == 0
    assert dut.io_in2_ack.value == 0

    # Serve a request on input 1 and leave input 2 alone
    dut.io_in1_req.value = 1
    await Edge(dut.io_out_req)
    await Timer(1)

    assert dut.io_out_req.value == 1
    assert dut.io_out_data.value == 42
    dut.io_out_ack.value = 1
    await Timer(1, "ns")

    assert dut.io_in1_ack.value == 1
    assert dut.io_in2_ack.value == 0

    # Serve a request on input 2
    dut.io_in2_req.value = 1
    await Edge(dut.io_out_req)
    await Timer(1)

    assert dut.io_out_req.value == 0
    assert dut.io_out_data.value == 84
    dut.io_out_ack.value = 0
    await Timer(1, "ns")

    assert dut.io_in1_ack.value == 1
    assert dut.io_in2_ack.value == 1

