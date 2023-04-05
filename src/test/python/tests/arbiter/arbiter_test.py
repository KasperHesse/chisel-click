import cocotb
from cocotb.triggers import Timer
from cocotb.triggers import Edge


@cocotb.test()
async def arbiter_test(dut):
    """It should arbitrate between two producers"""
    #More-or-less a copy of the rgd_mutex test, but also verifies that correct data is forwarded
    # Reset
    dut.io_in1_req.value = 0
    dut.io_in2_req.value = 0
    dut.io_in1_data.value = 42
    dut.io_in2_data.value = 84
    dut.io_out_ack.value = 0
    dut.reset.value = 1
    await Timer(3, "ns")
    dut.reset.value = 0
    await Timer(3, "ns")

    #Toggle in1, should toggle out_req and forward data from in1
    dut.io_in1_req.value = 1
    await Edge(dut.io_out_req)
    await Timer(1, "ns")
    assert dut.io_out_req.value == 1
    assert dut.io_out_data.value == 42
    await Timer(1, "ns")

    #Toggle ack, should propagate back to in1
    dut.io_out_ack.value = 1
    await Timer(1, "ns")
    assert dut.io_in1_ack.value == 1

    #Toggle in2, should toggle out_req and forward data from in2
    dut.io_in2_req.value = 1
    await Timer(3, "ns")
    assert dut.io_out_req.value == 0
    assert dut.io_out_data.value == 84
    await Timer(1, "ns")

    #Toggle in1 and modify data. Should not toggle output request, should keep output data from in2
    dut.io_in1_data.value = 43
    dut.io_in1_req.value = 0
    await Timer(1, "ns")
    assert dut.io_out_req.value == 0
    assert dut.io_out_data.value == 84
    await Timer(1, "ns")

    #Toggle ack. Should propagate to in2 and also toggle out_req and show data from i1n
    dut.io_out_ack.value = 0
    await Timer(3, "ns")
    assert dut.io_in2_ack.value == 1
    assert dut.io_out_req.value == 1
    assert dut.io_out_data.value == 43
