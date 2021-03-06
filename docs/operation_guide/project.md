# 建立合作

为了管理联邦学习建模任务中涉及到的数据集、模型、衍生数据集等资源，WeFe 提出了项目的概念，联邦学习建模活动基于项目为单位展开。

需要先建立项目，然后才能在项目内进行建模操作。

## 建立项目

由于联邦学习建模是多个成员使用多个数据集进行合作，所以在创建项目时需要选择至少一个协作方，这个协作方允许是自己。

项目中的成员和成员提供的数据集在项目创建后依然可以维护这些内容，所以无需在创建项目时过分纠结这些选项。


    ⚠️ 注意
    如果您即将进行的是纵向联邦学习，请务必由数据集有 label（y值） 的一方作为发起方创建项目。


<img src="_media/operation_guide/project_add.png" style="max-height:700px;border:1px solid #ccc" />

## 审核项目

作为项目的参与者，当发起方创建项目后，我方的项目列表会出现该项目。

这时需要我方对是否参与该项目进行审核，在同意参与该项目后，才成为项目的正式成员，只有正式成员能参与该项目中的联邦学习建模。

<img src="_media/operation_guide/project_audit.png" style="max-height:700px;border:1px solid #ccc" />


## 审核数据集

数据集需要被添加到项目中之后才能在项目中使用，除了各成员主动将数据集添加到项目外，还可以由发起方主动申请使用其他成员的数据集。

被申请的数据集需要对应的成员审核，只有数据集的拥有者同意后，数据集才能在项目中被使用。

<img src="_media/operation_guide/project_data_set_audit.png" style="max-height:700px;" />


## 审核成员

项目中成员的来源分为两种，一种是创建项目时指定的，这种情况只需要各成员自己同意加入项目，即可成为正式成员。

另外一种是项目创建成功之后再添加的，这种情况除了被添加成员自己同意之外，还需要项目中所有其他正式成员的同意。

<img src="_media/operation_guide/project_member_audit.png" style="max-height:700px;" />
