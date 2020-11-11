//
//  DBBaseView.m
//  DreamBox_iOS
//
//  Created by zhangchu on 2020/8/6.
//

#import "DBBaseView.h"
#import "DBParser.h"
#import "DBPool.h"
#import "NSArray+DBExtends.h"
#import "UIView+DBStrike.h"
#import <objc/runtime.h>

@interface DBBaseView()

@property (nonatomic, strong) NSMutableArray *kvoArrM;

@end

@implementation DBBaseView

- (void)dealloc{
    [self handleDismissOn:self.model.changeOn];
    if(self.kvoArrM.count > 0){
        NSDictionary *metaDict = [DBParser getMetaDictByPathId:_pathId];
        [self.kvoArrM enumerateObjectsUsingBlock:^(id  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
            [metaDict removeObserver:self forKeyPath:obj];
        }];
        [self.kvoArrM removeAllObjects];
    }
}

- (instancetype)initWithFrame:(CGRect)frame {
    if(self = [super initWithFrame:frame]){
        [self onCreateView];
    }
    return self;
}

- (void)onCreateView{
    
}

//-(DBBaseView *)onCreateViewWithPathId:(NSString *)pathId{
//    _pathId = pathId;
//    _accessKey = [[DBPool shareDBPool] getAccessKeyWithPathId:pathId];
//    return [[DBBaseView alloc] init];
//}

- (void)setDataWithModel:(DBViewModel *)model andPathId:(NSString *)pathId{
    _model = model;
    _pathId = pathId;
    _accessKey = [[DBPool shareDBPool] getAccessKeyWithPathId:pathId];
}

- (void)handleChangeOn:(NSString *)changeOnstr
{
    NSDictionary *metaDict = [DBParser getMetaDictByPathId:_pathId];
    if (!changeOnstr) {
        return;
    }
    [metaDict addObserver:self forKeyPath:changeOnstr options:NSKeyValueObservingOptionNew | NSKeyValueObservingOptionOld context:nil];
    [self.kvoArrM addObject:changeOnstr];
}

- (void)handleDismissOn:(NSString *)dismissOnStr
{
    NSDictionary *metaDict = [DBParser getMetaDictByPathId:_pathId];
    if (!dismissOnStr) {
        return;
    }
    [metaDict addObserver:self forKeyPath:dismissOnStr options:NSKeyValueObservingOptionNew | NSKeyValueObservingOptionOld context:nil];
    [self.kvoArrM addObject:dismissOnStr];
}

-(void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary<NSString *,id> *)change context:(void *)context
{
    if([keyPath isEqualToString:_model.changeOn]){
        [self reload];
    }
}

- (NSMutableArray *)kvoArrM{
    if(!_kvoArrM){
        _kvoArrM = [NSMutableArray new];
    }
    return _kvoArrM;
}

//展示时触发block中存储的事件
-(void)willMoveToSuperview:(UIView *)newSuperview
{
    [super willMoveToSuperview:newSuperview];
    if (self.viewVisible){
        self.viewVisible();
    }
}

//从父view移除时候调用
- (void)removeFromSuperview{
    [super removeFromSuperview];
    if (self.viewInVisible){
        self.viewInVisible();
    }
}


@end
